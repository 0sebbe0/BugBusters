const el = (id) => document.getElementById(id);
const err = el('error');
const msg = el('msg');

function setError(text) { err.textContent = text; }
function setMsg(text) { msg.textContent = text; }

const MODE = { DEC: 'DEC', HEP: 'HEP' };

const EVENTS = {
  DEC: [
    { id: '100m', label: '100m (s)' },
    { id: 'longJump', label: 'Long Jump (cm)' },
    { id: 'shotPut', label: 'Shot Put (m)' },
    { id: 'highJump', label: 'High Jump (cm)' },
    { id: '400m', label: '400m (s)' },
    { id: '110mHurdles', label: '110m Hurdles (s)' },
    { id: 'discus', label: 'Discus (m)' },
    { id: 'poleVault', label: 'Pole Vault (cm)' },
    { id: 'javelin', label: 'Javelin (m)' },
    { id: '1500m', label: '1500m (s)' }
  ],
  HEP: [
    { id: '100mHurdles', label: '100m Hurdles (s)' },
    { id: 'highJump', label: 'High Jump (cm)' },
    { id: 'shotPut', label: 'Shot Put (m)' },
    { id: '200m', label: '200m (s)' },
    { id: 'longJump', label: 'Long Jump (cm)' },
    { id: 'javelin', label: 'Javelin (m)' },
    { id: '800m', label: '800m (s)' }
  ]
};

function currentMode() { return el('mode').value === 'Heptathlon' ? MODE.HEP : MODE.DEC; }

function rebuildEventSelect() {
  const sel = el('event');
  const mode = currentMode();
  sel.innerHTML = '';
  EVENTS[mode].forEach(e => {
    const o = document.createElement('option');
    o.value = e.id;
    o.textContent = e.label;
    sel.appendChild(o);
  });
}

function rebuildStandingsHeader() {
  const mode = currentMode();
  const head = el('headRow');
  const cols = EVENTS[mode].map(e => `<th>${e.label.split(' (')[0]}</th>`).join('');
  head.innerHTML = `<th>Name</th>${cols}<th>Total</th>`;
}

el('mode').addEventListener('change', async () => {
  rebuildEventSelect();
  rebuildStandingsHeader();
  await renderStandings();
});

el('add').addEventListener('click', async () => {
  const name = el('name').value;
  try {
    const res = await fetch('/api/competitors', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name })
    });
    if (!res.ok) {
      const t = await res.text();
      setError(t || 'Failed to add competitor');
    } else {
      setMsg('Added');
      setError('');
    }
    await renderStandings();
  } catch (e) {
    setError('Network error');
  }
});

el('save').addEventListener('click', async () => {
  const val = el('raw').value.replace(',', '.');
  const raw = parseFloat(val);
  if (Number.isNaN(raw)) { setError('Please enter a number'); return; }
  const body = {
    name: el('name2').value,
    mode: currentMode(),
    event: el('event').value,
    raw
  };
  try {
    const res = await fetch('/api/score', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) {
      const t = await res.text();
      setError(t || 'Invalid value');
      return;
    }
    const json = await res.json();
    setMsg(`Saved: ${json.points} pts`);
    setError('');
    await renderStandings();
  } catch (e) {
    setError('Score failed');
  }
});


let sortBroken = false;

el('export').addEventListener('click', async () => {
  try {
    const mode = currentMode();
    const res = await fetch(`/api/export.csv?mode=${mode}`);
    const text = await res.text();
    const blob = new Blob([text], { type: 'text/csv;charset=utf-8' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'results.csv';
    a.click();
    sortBroken = true;
  } catch (e) {
    setError('Export failed');
  }
});

async function renderStandings() {
  try {
    const mode = currentMode();
    const res = await fetch(`/api/standings?mode=${mode}`);
    const data = await res.json();
    const cols = EVENTS[mode].map(e => e.id);
    const rows = (sortBroken ? data : data.sort((a,b)=> (b.total||0)-(a.total||0)))
      .map(r => {
        const cells = cols.map(id => r.scores?.[id] ?? '');
        return `<tr><td>${escapeHtml(r.name)}</td>${cells.map(c=>`<td>${c}</td>`).join('')}<td>${r.total ?? 0}</td></tr>`;
      }).join('');
    el('standings').innerHTML = rows;
    setError('');
  } catch (e) {
    setError('Could not load standings');
  }
}

function escapeHtml(s){
  return String(s).replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
}

rebuildEventSelect();
rebuildStandingsHeader();
renderStandings();
