// backend/app/static/app.js
const API = "";
const userId = () => document.getElementById("xUser").value;
const headers = () => ({ "X-User": userId(), "Content-Type": "application/json" });

async function j(url, opts = {}) {
  const resp = await fetch(API + url, { ...opts, headers: { ...headers(), ...(opts.headers || {}) } });
  if (resp.status === 204) return null;
  const body = await resp.json().catch(() => ({}));
  if (!resp.ok) throw new Error(body.detail || `HTTP ${resp.status}`);
  return body;
}

async function loadList() {
  const q = document.getElementById("search").value;
  const dept = document.getElementById("deptFilter").value;
  const params = new URLSearchParams({ page: "1", page_size: "50" });
  if (q) params.set("q", q);
  if (dept) params.set("department", dept);
  const data = await j("/api/employees?" + params.toString());
  const tbody = data.items.map(e => `
    <tr>
      <td>${e.employee_no}</td><td>${e.name}</td><td>${e.department || ""}</td>
      <td>${e.position || ""}</td><td>${e.status}</td>
      <td><button data-no="${e.employee_no}" class="edit">编辑</button>
          <button data-no="${e.employee_no}" class="del">删除</button></td>
    </tr>`).join("");
  document.getElementById("result").innerHTML =
    `<table><thead><tr><th>工号</th><th>姓名</th><th>部门</th><th>职位</th><th>状态</th><th>操作</th></tr></thead><tbody>${tbody}</tbody></table>`;
  document.querySelectorAll(".edit").forEach(b => b.onclick = () => openEdit(b.dataset.no));
  document.querySelectorAll(".del").forEach(b => b.onclick = () => remove(b.dataset.no));
}

async function addOrEdit(e) {
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = Object.fromEntries(fd.entries());
  if (payload.hire_date === "") delete payload.hire_date;
  try {
    if (editingNo) {
      await j(`/api/employees/${editingNo}`, { method: "PATCH", body: JSON.stringify(payload) });
    } else {
      await j("/api/employees", { method: "POST", body: JSON.stringify(payload) });
    }
    closeDlg(); await loadList();
  } catch (err) { alert(err.message); }
}

async function remove(no) {
  if (!confirm(`确认删除员工 ${no}？`)) return;
  try { await j(`/api/employees/${no}`, { method: "DELETE" }); await loadList(); }
  catch (err) { alert(err.message); }
}

async function importExcel() {
  const file = document.getElementById("fileInput").files[0];
  if (!file) { alert("请选择 xlsx 文件"); return; }
  const form = new FormData(); form.append("file", file);
  try {
    const resp = await fetch(API + "/api/imports", { method: "POST", headers: { "X-User": userId() }, body: form });
    const body = await resp.json();
    if (!resp.ok) { alert(body.detail || resp.status); return; }
    alert(`导入完成：成功 ${body.success}，失败 ${body.failed}\n` +
      body.rows.filter(r => !r.ok).map(r => `第${r.row_index}行(${r.employee_no}): ${r.error}`).join("\n"));
    await loadList();
  } catch (err) { alert(err.message); }
}

async function loadMyRole() { const u = await j("/api/users/me"); alert(`${u.username} 角色：${u.role}`); }

let editingNo = null;
function openAdd() { editingNo = null; document.getElementById("empForm").reset(); document.getElementById("dlg").showModal(); }
async function openEdit(no) {
  const e = await j(`/api/employees/${no}`);
  editingNo = no;
  const f = document.getElementById("empForm");
  f.employee_no.value = e.employee_no; f.name.value = e.name; f.department.value = e.department || "";
  f.position.value = e.position || ""; f.email.value = e.email || ""; f.phone.value = e.phone || "";
  f.hire_date.value = e.hire_date || ""; f.status.value = e.status;
  document.getElementById("dlg").showModal();
}
function closeDlg() { document.getElementById("dlg").close(); }

document.getElementById("empForm").addEventListener("submit", addOrEdit);
document.getElementById("btnAdd").onclick = openAdd;
document.getElementById("btnCancel").onclick = closeDlg;
document.getElementById("btnImport").onclick = importExcel;
document.getElementById("btnMe").onclick = loadMyRole;
document.getElementById("btnSearch").onclick = loadList;
async function downloadTemplate(fmt) {
  try {
    const resp = await fetch(`${API}/api/imports/template?format=${fmt}`, { headers: { "X-User": userId() } });
    if (!resp.ok) { const b = await resp.json().catch(() => ({})); throw new Error(b.detail || `HTTP ${resp.status}`); }
    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url; a.download = `employee_import_template.${fmt}`;
    a.click(); URL.revokeObjectURL(url);
  } catch (err) { alert(err.message); }
}

document.getElementById("btnTemplate").onclick = () => downloadTemplate("xlsx");
document.getElementById("btnTemplateCsv").onclick = () => downloadTemplate("csv");
loadList();