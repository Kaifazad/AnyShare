
package com.localshare.app.server

import android.content.Context

object WebUI {
    fun getHtml(deviceName: String, needsAuth: Boolean = false): String {
        val D = "$"
        return """
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>LocalShare</title>
<link id="favicon" rel="icon" type="image/png" href="/logo-dark.png">
<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
<style>
:root[data-theme="light"] {
    --bg: #FDFBFF;
    --text: #1A1B1F;
    --text-sec: #44474E;
    --accent: #0B57D0;
    --accent-hover: #0842A0;
    --surface: #F0F4F8;
    --surface-hover: #E1E2E8;
    --border: #E1E2E8;
    --danger: #B3261E;
    --card-bg: #FFFFFF;
    --toolbar-bg: #FFFFFF;
    --toolbar-shadow: rgba(0,0,0,0.1);
    --modal-bg: rgba(255,255,255,0.9);
}
:root[data-theme="dark"] {
    --bg: #1A1B1F;
    --text: #E3E2E6;
    --text-sec: #C4C6D0;
    --accent: #A8C7FA;
    --accent-hover: #D3E3FD;
    --surface: #282A2F;
    --surface-hover: #3F4045;
    --border: #44474E;
    --danger: #F2B8B5;
    --card-bg: #282A2F;
    --toolbar-bg: #282A2F;
    --toolbar-shadow: rgba(0,0,0,0.5);
    --modal-bg: rgba(0,0,0,0.8);
}

* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Roboto', sans-serif; }
body {
    background-color: var(--bg);
    color: var(--text);
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    transition: background-color 0.3s, color 0.3s;
}

/* Header */
header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: var(--bg);
    position: sticky;
    top: 0;
    z-index: 100;
}
.brand { display: flex; align-items: center; gap: 12px; font-weight: 500; font-size: 1.25rem; color: var(--text); }
.brand img { width: 32px; height: 32px; }
.header-actions { display: flex; align-items: center; gap: 16px; }

/* Status Indicator */
.status-indicator {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.85rem;
    font-weight: 500;
    color: var(--text-sec);
    background: var(--surface);
    padding: 8px 16px;
    border-radius: 24px;
}
.status-dot {
    width: 8px;
    height: 8px;
    background-color: #388E3C;
    border-radius: 50%;
}
:root[data-theme="dark"] .status-dot { background-color: #81C995; }

/* Theme Toggle */
.theme-btn {
    background: none; border: none; color: var(--text); cursor: pointer;
    width: 48px; height: 48px; border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    transition: background 0.2s;
}
.theme-btn:hover { background: var(--surface-hover); }

/* Main Content */
main { padding: 24px; flex-grow: 1; max-width: 1400px; margin: 0 auto; width: 100%; }

.stats-bar {
    display: flex; gap: 24px; margin-bottom: 24px;
}
.stat-item {
    display: flex; flex-direction: row; align-items: baseline; gap: 8px;
    background: var(--surface); padding: 16px 24px; border-radius: 16px;
}
.upload-item { cursor: pointer; transition: background 0.2s; align-items: center; }
.upload-item:hover { background: var(--surface-hover); }
.stat-value { font-size: 1.5rem; color: var(--accent); font-weight: 500; }
.stat-label { font-size: 1.1rem; color: var(--text-sec); font-weight: 500; }

/* Grid */
.grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
}
@media (max-width: 600px) {
    .grid { grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
    
    header { padding: 12px 16px; }
    .brand { font-size: 1.1rem; gap: 8px; }
    .brand img { width: 28px; height: 28px; }
    
    .stats-bar { flex-direction: column; gap: 12px; margin-bottom: 16px; }
    .stat-item { justify-content: space-between; padding: 12px 16px; }
    
    .toolbar { width: 90%; padding: 12px 16px; flex-direction: column; gap: 12px; transform: translate(-50%, 200px); }
    .toolbar.active { transform: translate(-50%, 0); }
    .toolbar .actions { width: 100%; justify-content: space-between; }
    .toolbar .actions .btn { flex-grow: 1; justify-content: center; text-align: center; font-size: 0.9rem; padding: 10px 12px; }
}

.file-card {
    background: var(--card-bg);
    border-radius: 16px;
    overflow: hidden;
    position: relative;
    cursor: pointer;
    border: 1px solid var(--border);
    transition: transform 0.2s, background-color 0.2s, box-shadow 0.2s;
}
.file-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.file-card.selected { background: var(--surface-hover); border-color: var(--accent); }

.file-preview {
    height: 140px;
    background: var(--surface);
    display: flex; align-items: center; justify-content: center;
    position: relative; overflow: hidden;
}
.file-preview img { width: 100%; height: 100%; object-fit: cover; }
.file-icon { font-size: 3rem; opacity: 0.8; }

.file-info { padding: 16px; }
.file-info h3 { font-size: 0.95rem; font-weight: 500; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; color: var(--text); white-space: normal; word-break: break-word; line-height: 1.2; }
.file-meta { font-size: 0.8rem; color: var(--text-sec); display: flex; justify-content: space-between; }

.checkbox {
    position: absolute; top: 12px; left: 12px;
    width: 24px; height: 24px; border-radius: 50%;
    border: 2px solid var(--text-sec);
    background: var(--modal-bg);
    z-index: 10; transition: all 0.2s;
}
.file-card.selected .checkbox {
    background: var(--accent); border-color: var(--accent);
}
.file-card.selected .checkbox::after {
    content: ''; position: absolute; left: 7px; top: 3px; width: 4px; height: 10px;
    border: solid var(--bg); border-width: 0 2px 2px 0; transform: rotate(45deg);
}

/* Toolbar */
.toolbar {
    position: fixed; bottom: 32px; left: 50%; transform: translate(-50%, 150px);
    background: var(--toolbar-bg);
    padding: 16px 24px; border-radius: 28px;
    display: flex; align-items: center; gap: 24px;
    box-shadow: 0 4px 20px var(--toolbar-shadow);
    transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    z-index: 200;
}
.toolbar.active { transform: translate(-50%, 0); }
.toolbar .selection-count { font-weight: 500; color: var(--accent); }
.toolbar .actions { display: flex; gap: 12px; }
.btn {
    background: var(--surface); color: var(--text); border: none;
    padding: 10px 20px; border-radius: 24px; cursor: pointer; font-size: 0.95rem;
    font-weight: 500; transition: all 0.2s;
}
.btn:hover { background: var(--surface-hover); }
.btn.primary { background: var(--accent); color: var(--bg); }
.btn.primary:hover { background: var(--accent-hover); }

/* Modals */
.modal-overlay {
    position: fixed; top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);
    display: none; justify-content: center; align-items: center; z-index: 1000;
}
.modal-overlay.active { display: flex; }
.modal-content {
    background: var(--card-bg);
    padding: 32px; border-radius: 28px; text-align: center; max-width: 400px; width: 90%;
    display: flex; flex-direction: column; align-items: center;
}
.modal-content img { width: 64px; height: 64px; margin-bottom: 16px; border-radius: 16px; }
.modal-content h2 { margin-bottom: 24px; font-weight: 500; font-size: 1.5rem; color: var(--text); }
.pin-input {
    width: 100%; padding: 16px; border-radius: 16px;
    border: 1px solid var(--border); background: var(--surface); color: var(--text);
    font-size: 1.5rem; text-align: center; letter-spacing: 12px; font-weight: 500;
    margin-bottom: 24px; outline: none; transition: border-color 0.2s;
}
.pin-input:focus { border-color: var(--accent); }

.media-container {
    width: 90vw; height: 90vh; max-width: 1200px;
    display: flex; justify-content: center; align-items: center; position: relative;
    background: var(--bg); border-radius: 24px; overflow: hidden;
}
.media-container img, .media-container video {
    max-width: 100%; max-height: 100%; border-radius: 24px;
}
.close-media {
    position: absolute; top: 16px; right: 16px; 
    background: rgba(0,0,0,0.5); border: none; border-radius: 50%;
    width: 48px; height: 48px;
    color: white; font-size: 1.5rem; cursor: pointer;
    display: flex; justify-content: center; align-items: center;
    backdrop-filter: blur(8px); z-index: 10;
}

#authScreen { display: ${if(needsAuth) "flex" else "none"}; }
#mainApp { display: ${if(needsAuth) "none" else "flex"}; flex-direction: column; min-height: 100vh; }
</style>
</head>
<body>

<div class="modal-overlay" id="authScreen">
    <div class="modal-content">
        <img id="lockLogo" src="/logo-dark.png" alt="Logo">
        <h1 style="margin-top: 16px; margin-bottom: 8px; color: var(--text);">LocalShare</h1>
        <h2 style="margin-top: 0; color: var(--text-sec);">Secured Access</h2>
        <input type="password" class="pin-input" id="pinInput" placeholder="••••" maxlength="8">
        <button class="btn primary" onclick="submitAuth()" style="width:100%">Unlock</button>
        <p id="authError" style="color:var(--danger);margin-top:16px;font-size:0.9rem;"></p>
    </div>
</div>

<div id="mainApp">
    <header>
        <div class="brand">
            <img id="headerLogo" src="/logo-dark.png" alt="Logo">
            LocalShare
        </div>
        <div class="header-actions">
            <div class="status-indicator">
                <div class="status-dot"></div>
                <span>Connected</span>
            </div>
            <button class="theme-btn" onclick="toggleTheme()" id="themeIcon">
                <!-- SVG replaced by JS -->
            </button>
        </div>
    </header>

    <main>
        <div class="stats-bar">
            <div class="stat-item">
                <span class="stat-label">Files - </span>
                <span class="stat-value" id="statFiles">0</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Total Size - </span>
                <span class="stat-value" id="statSize">0 B</span>
            </div>
            <div class="stat-item" id="e2eBadge" style="display:none">
                <span class="stat-label">Encrypted - </span>
                <span class="stat-value" style="color:#388E3C">AES-256</span>
            </div>
            <div style="flex-grow: 1;"></div>
            <div class="stat-item upload-item" onclick="document.getElementById('uploadInput').click()">
                <span class="stat-label">Upload</span>
                <span class="stat-value" style="display:flex;align-items:center;"><svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"/></svg></span>
            </div>
            <input type="file" id="uploadInput" multiple style="display:none;" onchange="handleFilesUpload(event)">
        </div>

        <div class="grid" id="grid">
            <!-- Cards injected via JS -->
        </div>
    </main>

    <div class="toolbar" id="toolbar">
        <div class="selection-count" id="selectionCount">0 selected</div>
        <div class="actions">
            <button class="btn" onclick="clearSelection()" style="display:flex; align-items:center; gap:8px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                Clear Selection
            </button>
            <button class="btn primary" onclick="downloadSelected()">Download</button>
        </div>
    </div>
</div>

<div class="modal-overlay" id="mediaModal" onclick="closeModal(event)">
    <div class="media-container" id="mediaContainerBlock">
        <button class="close-media" onclick="closeModal(event)">×</button>
        <div id="modalContent" style="display:flex;justify-content:center;align-items:center;width:100%;height:100%"></div>
    </div>
</div>

<script>
// Theme Management
const savedTheme = localStorage.getItem('theme') || 'dark';
document.documentElement.setAttribute('data-theme', savedTheme);

function updateThemeIcon(isLight) {
    document.getElementById('themeIcon').innerHTML = isLight ? 
        `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>` : 
        `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`;
        
    const logoSrc = isLight ? '/logo.png' : '/logo-dark.png';
    const lockLogo = document.getElementById('lockLogo');
    if (lockLogo) lockLogo.src = logoSrc;
    const headerLogo = document.getElementById('headerLogo');
    if (headerLogo) headerLogo.src = logoSrc;
    const favicon = document.getElementById('favicon');
    if (favicon) favicon.href = logoSrc;
}
updateThemeIcon(savedTheme === 'light');

function toggleTheme() {
    const root = document.documentElement;
    const isLight = root.getAttribute('data-theme') === 'light';
    root.setAttribute('data-theme', isLight ? 'dark' : 'light');
    localStorage.setItem('theme', isLight ? 'dark' : 'light');
    updateThemeIcon(!isLight);
}

// Data State
let filesData = [];
let selectedIds = new Set();
let encryptionKey = null;
let lastFilesStr = '';

function base64ToUint8Array(base64Str) {
    let b64 = base64Str.replace(/-/g, '+').replace(/_/g, '/');
    while (b64.length % 4) { b64 += '='; }
    const raw = window.atob(b64);
    const result = new Uint8Array(new ArrayBuffer(raw.length));
    for (let i = 0; i < raw.length; i++) {
        result[i] = raw.charCodeAt(i);
    }
    return result;
}

// Polling
function setupPolling() {
    setTimeout(async () => {
        if(document.getElementById('mainApp').style.display !== 'none') {
            await fetchFiles(true);
        }
        setupPolling();
    }, 2000);
}

async function fetchFiles(silent = false) {
    try {
        const res = await fetch('/api/files');
        if(res.status === 401) {
            if(!silent) {
                document.getElementById('authScreen').style.display = 'flex';
                document.getElementById('mainApp').style.display = 'none';
            }
            return;
        }
        const data = await res.json();
        
        const newStr = JSON.stringify(data.files || []);
        if (newStr !== lastFilesStr) {
            filesData = data.files || [];
            lastFilesStr = newStr;
            updateStats();
            renderGrid();
        }

        if(data.encrypted) {
            document.getElementById('e2eBadge').style.display = 'flex';
            if(!encryptionKey) fetchKey();
        } else {
            document.getElementById('e2eBadge').style.display = 'none';
            encryptionKey = null;
        }
    } catch(e) { if(!silent) console.error('Fetch error', e); }
}

function updateStats() {
    document.getElementById('statFiles').textContent = filesData.length;
    let totalBytes = filesData.reduce((acc, f) => acc + (f.size || 0), 0);
    const units = ['B', 'KB', 'MB', 'GB'];
    let u = 0;
    while(totalBytes >= 1024 && u < units.length - 1) { totalBytes /= 1024; u++; }
    document.getElementById('statSize').textContent = `${D}{totalBytes.toFixed(1)} ${D}{units[u]}`;
}

async function fetchKey() {
    try {
        const res = await fetch('/api/encryption-key');
        const data = await res.json();
        if(data.key) encryptionKey = data.key;
    } catch(e) {}
}

function getIconForType(typeIcon) {
    const map = {
        'video': '🎥', 'image': '🖼️', 'audio': '🎵', 
        'document': '📄', 'pdf': '📕', 'archive': '📦', 
        'android': '🤖', 'folder': '📁', 'file': '📎'
    };
    return map[typeIcon] || '📎';
}

function renderGrid() {
    const grid = document.getElementById('grid');
    grid.innerHTML = filesData.map((f, index) => {
        const isSelected = selectedIds.has(f.id);
        const hasThumbnail = f.typeIcon === 'image' || f.typeIcon === 'video' || f.typeIcon === 'pdf' || f.typeIcon === 'android';
        
        let previewHtml = `<div class="file-icon">${D}{getIconForType(f.typeIcon)}</div>`;
        if (hasThumbnail) {
            let thumbUrl = f.typeIcon === 'android' ? `/api/icon/${D}{f.id}` : `/api/thumbnail/${D}{f.id}`;
            previewHtml = `<img src="${D}{thumbUrl}" loading="lazy" onerror="this.outerHTML='<div class=\'file-icon\'>${D}{getIconForType(f.typeIcon)}</div>'">`;
        }
        
        let showPlay = f.isStreamable && (f.mimeType.startsWith('video/') || f.mimeType.startsWith('audio/'));

        return `
            <div class="file-card ${D}{isSelected ? 'selected' : ''}" onclick="toggleSelect('${D}{f.id}', event)">
                <div class="checkbox"></div>
                <div class="file-preview" onclick="event.stopPropagation(); if('${D}{f.isStreamable}'==='true' || '${D}{f.typeIcon}'==='image' || '${D}{f.typeIcon}'==='pdf') openMedia('${D}{f.id}', '${D}{f.mimeType}')">
                    ${D}{previewHtml}
                    ${D}{showPlay ? '<div style="position:absolute;background:rgba(0,0,0,0.5);border-radius:50%;width:48px;height:48px;display:flex;align-items:center;justify-content:center;"><svg width="24" height="24" viewBox="0 0 24 24" fill="white"><path d="M8 5v14l11-7z"/></svg></div>' : ''}
                </div>
                <div class="file-info" style="display:flex; justify-content:space-between; align-items:center;">
                    <div style="overflow:hidden;">
                        <h3>${D}{f.name}</h3>
                        <div class="file-meta">
                            <span>${D}{f.typeIcon.toUpperCase()}</span>
                            <span style="margin-left:8px;">${D}{formatSize(f.size)}</span>
                        </div>
                    </div>
                    <button class="btn" style="padding:8px; border-radius:50%; width:36px; height:36px; display:flex; align-items:center; justify-content:center; flex-shrink:0; background:var(--surface);" onclick="event.stopPropagation(); downloadSingle('${D}{f.id}', '${D}{f.name}')">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                    </button>
                </div>
            </div>
        `;
    }).join('');
    updateToolbar();
}

async function downloadSingle(id, name) {
    if(encryptionKey) {
        await decryptAndDownload(id, name);
    } else {
        window.location.href = `/download/${D}{id}`;
    }
}

function formatSize(bytes) {
    if (!bytes) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    let u = 0;
    while(bytes >= 1024 && u < units.length - 1) { bytes /= 1024; u++; }
    return `${D}{bytes.toFixed(1)} ${D}{units[u]}`;
}

function toggleSelect(id, e) {
    const card = e.currentTarget;
    if (selectedIds.has(id)) {
        selectedIds.delete(id);
        if (card) card.classList.remove('selected');
    } else {
        selectedIds.add(id);
        if (card) card.classList.add('selected');
    }
    updateToolbar();
}

function clearSelection() {
    selectedIds.clear();
    document.querySelectorAll('.file-card.selected').forEach(c => c.classList.remove('selected'));
    updateToolbar();
}

function updateToolbar() {
    const tb = document.getElementById('toolbar');
    const cnt = document.getElementById('selectionCount');
    if (selectedIds.size > 0) {
        tb.classList.add('active');
        cnt.textContent = `${D}{selectedIds.size} selected`;
    } else {
        tb.classList.remove('active');
    }
}

async function downloadSelected() {
    if (selectedIds.size === 0) return;
    if (selectedIds.size === 1) {
        const id = Array.from(selectedIds)[0];
        const f = filesData.find(x => x.id == id);
        if(!f) return;
        if(encryptionKey) {
            await decryptAndDownload(id, f.name);
        } else {
            window.location.href = `/download/${D}{id}`;
        }
    } else {
        const ids = Array.from(selectedIds).join(',');
        if (encryptionKey) {
            await decryptAndDownloadZip(ids, 'LocalShare.zip');
        } else {
            window.location.href = `/api/download-zip?ids=${D}{ids}`;
        }
    }
    clearSelection();
}

async function decryptAndDownload(id, filename) {
    if (!window.crypto || !window.crypto.subtle) {
        alert("Your browser blocks decryption over HTTP. Please disable encryption in the LocalShare Android app to download files on this device.");
        return;
    }
    try {
        const res = await fetch(`/download/${D}{id}`);
        const encryptedData = await res.arrayBuffer();
        
        const keyMaterial = await window.crypto.subtle.importKey(
            "raw",
            new Uint8Array(base64ToUint8Array(encryptionKey)),
            { name: "AES-GCM" },
            false,
            ["decrypt"]
        );
        
        const iv = encryptedData.slice(0, 12);
        const data = encryptedData.slice(12);
        
        const decryptedContent = await window.crypto.subtle.decrypt(
            { name: "AES-GCM", iv: new Uint8Array(iv) },
            keyMaterial,
            data
        );
        
        const blob = new Blob([decryptedContent]);
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    } catch(e) {
        alert("Decryption failed!");
        console.error(e);
    }
}

async function decryptAndDownloadZip(ids, filename) {
    if (!window.crypto || !window.crypto.subtle) {
        alert("Your browser blocks decryption over HTTP. Please disable encryption in the LocalShare Android app to download files on this device.");
        return;
    }
    try {
        const res = await fetch(`/api/download-zip?ids=${D}{ids}`);
        const encryptedData = await res.arrayBuffer();
        
        const keyMaterial = await window.crypto.subtle.importKey(
            "raw",
            new Uint8Array(base64ToUint8Array(encryptionKey)),
            { name: "AES-GCM" },
            false,
            ["decrypt"]
        );
        
        const iv = encryptedData.slice(0, 12);
        const data = encryptedData.slice(12);
        
        const decryptedContent = await window.crypto.subtle.decrypt(
            { name: "AES-GCM", iv: new Uint8Array(iv) },
            keyMaterial,
            data
        );
        
        const blob = new Blob([decryptedContent], {type: "application/zip"});
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(url);
    } catch(e) {
        alert("Decryption failed!");
    }
}

function openMedia(id, mime) {
    const m = document.getElementById('mediaModal');
    const c = document.getElementById('modalContent');
    const url = `/stream/${D}{id}`;
    
    if (mime.startsWith('video/')) {
        c.innerHTML = `<video src="${D}{url}" controls autoplay></video>`;
    } else if (mime.startsWith('audio/')) {
        c.innerHTML = `<audio src="${D}{url}" controls autoplay></audio>`;
    } else if (mime === 'application/pdf') {
        c.innerHTML = `<iframe src="${D}{url}" width="100%" height="100%" style="border:none;border-radius:24px;"></iframe>`;
    } else {
        c.innerHTML = `<img src="${D}{url}">`;
    }
    m.classList.add('active');
}

function closeModal(e) {
    // Only close if clicking outside or on close button
    if (e.target.classList.contains('modal-overlay') || e.target.classList.contains('close-media')) {
        document.getElementById('mediaModal').classList.remove('active');
        document.getElementById('modalContent').innerHTML = '';
    }
}

async function submitAuth() {
    const pin = document.getElementById('pinInput').value;
    if(!pin) return;
    
    try {
        const res = await fetch('/api/auth', {
            method: 'POST',
            body: JSON.stringify({pin})
        });
        const data = await res.json();
        
        if (data.success) {
            document.getElementById('authScreen').style.display = 'none';
            document.getElementById('mainApp').style.display = 'flex';
            fetchFiles();
        } else {
            document.getElementById('authError').textContent = data.error || 'Authentication failed';
        }
    } catch(e) {
        document.getElementById('authError').textContent = 'Connection error';
    }
}

// Initial fetch
fetchFiles();
setupPolling();

// Press Enter to submit PIN
document.getElementById('pinInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') submitAuth();
});

async function uploadFiles(files) {
    if (!files || files.length === 0) return;
    
    if (encryptionKey && (!window.crypto || !window.crypto.subtle)) {
        alert("Your browser blocks encryption over HTTP. Please disable encryption in the LocalShare Android app to upload files from this device.");
        return;
    }
    
    const originalText = document.getElementById('statFiles').textContent;
    document.getElementById('statFiles').textContent = "Uploading...";
    
    const formData = new FormData();
    let appendedCount = 0;
    for (let i = 0; i < files.length; i++) {
        let file = files[i];
        if (encryptionKey) {
            try {
                const buffer = await file.arrayBuffer();
                const keyMaterial = await window.crypto.subtle.importKey(
                    "raw",
                    new Uint8Array(base64ToUint8Array(encryptionKey)),
                    { name: "AES-GCM" },
                    false,
                    ["encrypt"]
                );
                
                const iv = window.crypto.getRandomValues(new Uint8Array(12));
                const encryptedContent = await window.crypto.subtle.encrypt(
                    { name: "AES-GCM", iv: iv },
                    keyMaterial,
                    buffer
                );
                
                const combinedData = new Uint8Array(iv.length + encryptedContent.byteLength);
                combinedData.set(iv, 0);
                combinedData.set(new Uint8Array(encryptedContent), iv.length);
                
                file = new Blob([combinedData]);
            } catch(e) {
                console.error("Encryption failed for file", files[i].name, e);
                alert("Failed to encrypt " + files[i].name);
                continue;
            }
        }
        formData.append("file" + i, file, files[i].name);
        formData.append("filename", files[i].name);
        appendedCount++;
    }
    
    if (appendedCount === 0) {
        document.getElementById('statFiles').textContent = originalText;
        return;
    }
    
    try {
        const res = await fetch("/api/upload", {
            method: "POST",
            body: formData
        });
        const data = await res.json();
        if (data.success) {
            fetchFiles(); // refresh list
        } else {
            alert("Upload failed: " + data.error);
            document.getElementById('statFiles').textContent = originalText;
        }
    } catch(e) {
        alert("Upload error!");
        console.error(e);
        document.getElementById('statFiles').textContent = originalText;
    }
}

async function handleFilesUpload(event) {
    const files = event.target.files;
    await uploadFiles(files);
    event.target.value = ''; // reset input
}

// Drag and Drop
const mainApp = document.getElementById('mainApp');
mainApp.addEventListener('dragover', (e) => {
    e.preventDefault();
    mainApp.style.opacity = "0.7";
});
mainApp.addEventListener('dragleave', (e) => {
    e.preventDefault();
    mainApp.style.opacity = "1";
});
mainApp.addEventListener('drop', (e) => {
    e.preventDefault();
    mainApp.style.opacity = "1";
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
        uploadFiles(e.dataTransfer.files);
    }
});

</script>
</body>
</html>"""
    }
}