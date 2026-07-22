
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

// ─── AES-256-GCM Polyfill (for HTTP contexts where crypto.subtle is blocked) ───
(function(){
  if(window.crypto&&window.crypto.subtle)return;
  if(!window.crypto)window.crypto={};
  var S=[99,124,119,123,242,107,111,197,48,1,103,43,254,215,171,118,202,130,201,125,250,89,71,240,173,212,162,175,156,164,114,192,183,253,147,38,54,63,247,204,52,165,229,241,113,216,49,21,4,199,35,195,24,150,5,154,7,18,128,226,235,39,178,117,9,131,44,26,27,110,90,160,82,59,214,179,41,227,47,132,83,209,0,237,32,252,177,91,106,203,190,57,74,76,88,207,208,239,170,251,67,77,51,133,69,249,2,127,80,60,159,168,81,163,64,143,146,157,56,245,188,182,218,33,16,255,243,210,205,12,19,236,95,151,68,23,196,167,126,61,100,93,25,115,96,129,79,220,34,42,144,136,70,238,184,20,222,94,11,219,224,50,58,10,73,6,36,92,194,211,172,98,145,149,228,121,231,200,55,109,141,213,78,169,108,86,244,234,101,122,174,8,186,120,37,46,28,166,180,198,232,221,116,31,75,189,139,138,112,62,181,102,72,3,246,14,97,53,87,185,134,193,29,158,225,248,152,17,105,217,142,148,155,30,135,233,206,85,40,223,140,161,137,13,191,230,66,104,65,153,45,15,176,84,187,22];
  var Rc=[1,2,4,8,16,32,64,128,27,54];
  var xt=function(a){return((a<<1)^(((a>>7)&1)*0x1b))&0xff;};
  function subB(s){for(var i=0;i<16;i++)s[i]=S[s[i]];}
  function shR(s){var t=s[1];s[1]=s[5];s[5]=s[9];s[9]=s[13];s[13]=t;t=s[2];s[2]=s[10];s[10]=t;t=s[6];s[6]=s[14];s[14]=t;t=s[15];s[15]=s[11];s[11]=s[7];s[7]=s[3];s[3]=t;}
  function mxC(s){for(var i=0;i<16;i+=4){var a=s[i],b=s[i+1],c=s[i+2],d=s[i+3],e=a^b^c^d;s[i]^=e^xt(a^b);s[i+1]^=e^xt(b^c);s[i+2]^=e^xt(c^d);s[i+3]^=e^xt(d^a);}}
  function aRK(s,w,o){for(var i=0;i<16;i++)s[i]^=w[o+i];}
  function kExp(k){var Nk=k.length/4,Nr=Nk+6,W=new Uint8Array(16*(Nr+1));for(var i=0;i<k.length;i++)W[i]=k[i];for(var i=Nk;i<4*(Nr+1);i++){var t=[W[(i-1)*4],W[(i-1)*4+1],W[(i-1)*4+2],W[(i-1)*4+3]];if(i%Nk===0){var tmp=t[0];t[0]=S[t[1]]^Rc[i/Nk-1];t[1]=S[t[2]];t[2]=S[t[3]];t[3]=S[tmp];}else if(Nk>6&&i%Nk===4){t[0]=S[t[0]];t[1]=S[t[1]];t[2]=S[t[2]];t[3]=S[t[3]];}W[i*4]=W[(i-Nk)*4]^t[0];W[i*4+1]=W[(i-Nk)*4+1]^t[1];W[i*4+2]=W[(i-Nk)*4+2]^t[2];W[i*4+3]=W[(i-Nk)*4+3]^t[3];}return{w:W,nr:Nr};}
  function aesBlk(b,ek){var s=new Uint8Array(b);aRK(s,ek.w,0);for(var r=1;r<ek.nr;r++){subB(s);shR(s);mxC(s);aRK(s,ek.w,r*16);}subB(s);shR(s);aRK(s,ek.w,ek.nr*16);return s;}
  function ghMul(X,Y){var Z=new Uint8Array(16),V=new Uint8Array(Y);for(var i=0;i<128;i++){if((X[i>>>3]>>(7-(i&7)))&1){for(var j=0;j<16;j++)Z[j]^=V[j];}var lb=V[15]&1;for(var j=15;j>0;j--)V[j]=(V[j]>>>1)|((V[j-1]&1)<<7);V[0]>>>=1;if(lb)V[0]^=0xe1;}return Z;}
  function ghash(H,d){var Y=new Uint8Array(16);for(var i=0;i<d.length;i+=16){var bl=new Uint8Array(16);for(var j=0;j<16&&i+j<d.length;j++)bl[j]=d[i+j];for(var j=0;j<16;j++)Y[j]^=bl[j];Y=ghMul(Y,H);}return Y;}
  function incCtr(c){for(var i=15;i>=12;i--){c[i]=(c[i]+1)&0xff;if(c[i]!==0)break;}}
  function gcmEnc(key,iv,pt){
    var ek=kExp(key),H=aesBlk(new Uint8Array(16),ek);
    var J0=new Uint8Array(16);for(var i=0;i<12;i++)J0[i]=iv[i];J0[15]=1;
    var ct=new Uint8Array(pt.length),cb=new Uint8Array(J0);
    for(var i=0;i<pt.length;i+=16){incCtr(cb);var eb=aesBlk(cb,ek);for(var j=0;j<16&&i+j<pt.length;j++)ct[i+j]=pt[i+j]^eb[j];}
    var pl=Math.ceil(ct.length/16)*16,ad=new Uint8Array(pl+16);ad.set(ct);
    var bits=ct.length*8,bH=Math.floor(bits/0x100000000),bL=bits>>>0;
    ad[pl+8]=(bH>>>24)&0xff;ad[pl+9]=(bH>>>16)&0xff;ad[pl+10]=(bH>>>8)&0xff;ad[pl+11]=bH&0xff;
    ad[pl+12]=(bL>>>24)&0xff;ad[pl+13]=(bL>>>16)&0xff;ad[pl+14]=(bL>>>8)&0xff;ad[pl+15]=bL&0xff;
    var tag=ghash(H,ad),E0=aesBlk(J0,ek);for(var i=0;i<16;i++)tag[i]^=E0[i];
    var out=new Uint8Array(ct.length+16);out.set(ct);out.set(tag,ct.length);return out;
  }
  function gcmDec(key,iv,data){
    var ek=kExp(key),H=aesBlk(new Uint8Array(16),ek);
    var ct=data.slice(0,data.length-16),rTag=data.slice(data.length-16);
    var J0=new Uint8Array(16);for(var i=0;i<12;i++)J0[i]=iv[i];J0[15]=1;
    var pl=Math.ceil(ct.length/16)*16,ad=new Uint8Array(pl+16);ad.set(ct);
    var bits=ct.length*8,bH=Math.floor(bits/0x100000000),bL=bits>>>0;
    ad[pl+8]=(bH>>>24)&0xff;ad[pl+9]=(bH>>>16)&0xff;ad[pl+10]=(bH>>>8)&0xff;ad[pl+11]=bH&0xff;
    ad[pl+12]=(bL>>>24)&0xff;ad[pl+13]=(bL>>>16)&0xff;ad[pl+14]=(bL>>>8)&0xff;ad[pl+15]=bL&0xff;
    var cTag=ghash(H,ad),E0=aesBlk(J0,ek);for(var i=0;i<16;i++)cTag[i]^=E0[i];
    var ok=true;for(var i=0;i<16;i++)if(cTag[i]!==rTag[i])ok=false;
    if(!ok)throw new Error("AES-GCM auth tag mismatch");
    var pt=new Uint8Array(ct.length),cb=new Uint8Array(J0);
    for(var i=0;i<ct.length;i+=16){incCtr(cb);var eb=aesBlk(cb,ek);for(var j=0;j<16&&i+j<ct.length;j++)pt[i+j]=ct[i+j]^eb[j];}
    return pt;
  }
  window.crypto.subtle={
    importKey:async function(f,d){return{_r:new Uint8Array(d)};},
    encrypt:async function(a,k,d){return gcmEnc(k._r,new Uint8Array(a.iv),new Uint8Array(d)).buffer;},
    decrypt:async function(a,k,d){return gcmDec(k._r,new Uint8Array(a.iv),new Uint8Array(d)).buffer;}
  };
  if(!window.crypto.getRandomValues){window.crypto.getRandomValues=function(a){for(var i=0;i<a.length;i++)a[i]=Math.floor(Math.random()*256);return a;};}
})();
// ─── End AES-GCM Polyfill ───

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
    const icons = {
        'pdf': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#e53935"/><path d="M14 30h20M14 36h14" stroke="#fff" stroke-width="2" stroke-linecap="round"/><text x="24" y="22" text-anchor="middle" font-size="12" font-weight="bold" fill="#fff">PDF</text></svg>',
        'document': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#1e88e5"/><path d="M14 16h20M14 22h20M14 28h16M14 34h12" stroke="#fff" stroke-width="2" stroke-linecap="round"/></svg>',
        'archive': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#f9a825"/><rect x="20" y="8" width="8" height="4" rx="1" fill="#fff" opacity=".7"/><rect x="20" y="14" width="8" height="4" rx="1" fill="#fff" opacity=".7"/><rect x="20" y="20" width="8" height="4" rx="1" fill="#fff" opacity=".7"/><rect x="18" y="26" width="12" height="14" rx="2" fill="#fff" opacity=".8"/><circle cx="24" cy="33" r="2" fill="#f9a825"/></svg>',
        'audio': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#7b1fa2"/><circle cx="24" cy="26" r="8" fill="none" stroke="#fff" stroke-width="2"/><circle cx="24" cy="26" r="3" fill="#fff"/><path d="M24 18v-6" stroke="#fff" stroke-width="2" stroke-linecap="round"/></svg>',
        'video': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#00897b"/><polygon points="20,18 32,26 20,34" fill="#fff"/></svg>',
        'image': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#43a047"/><circle cx="18" cy="16" r="4" fill="#fff" opacity=".8"/><path d="M8 34l10-10 6 6 8-8 8 8v10a4 4 0 0 1-4 4H12a4 4 0 0 1-4-4z" fill="#fff" opacity=".5"/></svg>',
        'android': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#4caf50"/><path d="M16 28h16v8a4 4 0 0 1-4 4h-8a4 4 0 0 1-4-4z" fill="#fff" opacity=".8"/><circle cx="20" cy="22" r="2" fill="#fff"/><circle cx="28" cy="22" r="2" fill="#fff"/></svg>',
        'folder': '<svg viewBox="0 0 48 48" width="48" height="48"><path d="M6 12a4 4 0 0 1 4-4h10l4 4h14a4 4 0 0 1 4 4v24a4 4 0 0 1-4 4H10a4 4 0 0 1-4-4z" fill="#ffa726"/></svg>',
        'file': '<svg viewBox="0 0 48 48" width="48" height="48"><rect x="8" y="2" width="32" height="44" rx="4" fill="#78909c"/><path d="M14 16h20M14 22h20M14 28h16" stroke="#fff" stroke-width="2" stroke-linecap="round"/></svg>'
    };
    return icons[typeIcon] || icons['file'];
}

function renderGrid() {
    const grid = document.getElementById('grid');
    grid.innerHTML = filesData.map((f, index) => {
        const isSelected = selectedIds.has(f.id);
        const hasThumbnail = f.typeIcon === 'image' || f.typeIcon === 'video' || f.typeIcon === 'android';
        
        let previewHtml = `<div class="file-icon">${D}{getIconForType(f.typeIcon)}</div>`;
        if (hasThumbnail) {
            let thumbUrl = f.typeIcon === 'android' ? `/api/icon/${D}{f.id}` : `/api/thumbnail/${D}{f.id}`;
            let fallbackIcon = getIconForType(f.typeIcon).replace(/"/g, '&quot;');
            previewHtml = `<img src="${D}{thumbUrl}" loading="lazy" onerror="this.outerHTML='<div class=&quot;file-icon&quot;>${D}{fallbackIcon}</div>'">`;
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