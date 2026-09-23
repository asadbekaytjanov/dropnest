const usernameEl = document.getElementById('username');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const emptyState = document.getElementById('emptyState');
const loadingState = document.getElementById('loadingState');
const fileInput = document.getElementById('fileInput');
const uploadBtn = document.getElementById('uploadBtn');
const dropZone = document.getElementById('dropZone');
const galleryEl = document.getElementById('gallery');
const logoutBtn = document.getElementById('logoutBtn');
const searchInput = document.getElementById('searchInput');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const pageIndicator = document.getElementById('pageIndicator');
const typeFilter = document.getElementById('typeFilter');
const sortBy = document.getElementById('sortBy');
const uploadPanel = document.getElementById('uploadPanel');
const uploadFileName = document.getElementById('uploadFileName');
const progressBar = document.getElementById('progressBar');
const cancelUploadBtn = document.getElementById('cancelUploadBtn');
const quotaCard = document.getElementById('quotaCard');
const quotaStatus = document.getElementById('quotaStatus');
const quotaUsageText = document.getElementById('quotaUsageText');
const quotaPercentText = document.getElementById('quotaPercentText');
const quotaProgressBar = document.getElementById('quotaProgressBar');
const quotaUsedText = document.getElementById('quotaUsedText');
const quotaRemainingText = document.getElementById('quotaRemainingText');
const quotaTotalText = document.getElementById('quotaTotalText');

let currentPage = 0;
let searchTerm = '';
const pageSize = 12;
let lastPageData = null;
let activeXhr = null;

/* Drag overlay state */
let dragCounter = 0;

/* ===== Error Handling ===== */

class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

function extractBackendMessage(payload) {
    if (!payload) {
        return null;
    }

    if (typeof payload === 'string') {
        return payload.trim() || null;
    }

    // Supports common global exception response formats:
    // { message: "..." }
    // { error: "..." }
    // { detail: "..." }
    // { errors: [...] }
    if (typeof payload.message === 'string' && payload.message.trim()) {
        return payload.message;
    }

    if (typeof payload.error === 'string' && payload.error.trim()) {
        return payload.error;
    }

    if (typeof payload.detail === 'string' && payload.detail.trim()) {
        return payload.detail;
    }

    if (Array.isArray(payload.errors) && payload.errors.length > 0) {
        return payload.errors
            .map(error => {
                if (typeof error === 'string') return error;
                return error.message || error.defaultMessage || String(error);
            })
            .join(', ');
    }

    return null;
}

async function getResponseErrorMessage(response) {
    const fallback = `Request failed with status ${response.status}`;

    try {
        const contentType =
            response.headers.get('content-type') || '';

        if (contentType.includes('application/json')) {
            const payload = await response.json();

            return extractBackendMessage(payload) || fallback;
        }

        const text = await response.text();

        return extractBackendMessage(text) || fallback;

    } catch {
        return fallback;
    }
}

function getXhrErrorMessage(xhr) {
    const fallback = `Upload failed with status ${xhr.status}`;

    if (!xhr.responseText) {
        return fallback;
    }

    try {
        const contentType =
            xhr.getResponseHeader('content-type') || '';

        if (contentType.includes('application/json')) {
            const payload = JSON.parse(xhr.responseText);

            return extractBackendMessage(payload) || fallback;
        }

        return extractBackendMessage(xhr.responseText) || fallback;

    } catch {
        return fallback;
    }
}

/* ===== Auth & API ===== */

function getToken() {
    return localStorage.getItem('token');
}

function getUsername() {
    return localStorage.getItem('username');
}

function goToLogin() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    window.location.href = 'login.html';
}

async function apiFetch(url, options = {}) {
    const token = getToken();

    if (!token) {
        goToLogin();
        throw new ApiError('No authentication token', 401);
    }

    const headers = new Headers(options.headers || {});
    headers.set('Authorization', 'Bearer ' + token);

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        goToLogin();
        throw new ApiError(
            'Your session has expired. Please log in again.',
            response.status
        );
    }

    if (!response.ok) {
        const message = await getResponseErrorMessage(response);

        throw new ApiError(message, response.status);
    }

    return response;
}

/* ===== UI Messages ===== */

function showError(message) {
    const textEl = errorMessage.querySelector('.msg-text') || errorMessage;
    textEl.textContent = message;
    errorMessage.style.display = 'flex';
    successMessage.style.display = 'none';
}

function showSuccess(message) {
    const textEl = successMessage.querySelector('.msg-text') || successMessage;
    textEl.textContent = message;
    successMessage.style.display = 'flex';
    errorMessage.style.display = 'none';
}

function hideMessages() {
    errorMessage.style.display = 'none';
    successMessage.style.display = 'none';
}
window.hideMessages = hideMessages;

function showLoading(isLoading) {
    loadingState.style.display = isLoading ? 'block' : 'none';
}

/* ===== File Utilities ===== */

function classifyType(contentType = '') {
    if (contentType.startsWith('image/')) return 'image';
    if (contentType.startsWith('video/')) return 'video';
    if (contentType.includes('pdf')) return 'pdf';
    if (contentType.includes('zip') || contentType.includes('tar') || contentType.includes('gzip') || contentType.includes('rar') || contentType.includes('7z')) return 'archive';
    return 'other';
}

function formatBytes(bytes = 0) {
    if (bytes < 1024) return `${bytes} B`;
    const kb = bytes / 1024;
    if (kb < 1024) return `${kb.toFixed(1)} KB`;
    const mb = kb / 1024;
    if (mb < 1024) return `${mb.toFixed(1)} MB`;
    return `${(mb / 1024).toFixed(1)} GB`;
}

function resetQuotaStateClasses() {
    quotaCard.classList.remove('state-warning', 'state-danger', 'state-full');
}

function setQuotaLoading() {
    resetQuotaStateClasses();
    quotaStatus.textContent = 'Loading...';
    quotaUsageText.textContent = '-- / --';
    quotaPercentText.textContent = '--%';
    quotaUsedText.textContent = '--';
    quotaRemainingText.textContent = '--';
    quotaTotalText.textContent = '--';
    quotaProgressBar.style.width = '0%';
    quotaProgressBar.setAttribute('aria-valuenow', '0');
    quotaProgressBar.setAttribute('aria-valuetext', 'Storage usage loading');
}

function setQuotaUnavailable(message) {
    resetQuotaStateClasses();
    quotaStatus.textContent = message;
    quotaUsageText.textContent = 'Quota unavailable';
    quotaPercentText.textContent = '--';
    quotaUsedText.textContent = '--';
    quotaRemainingText.textContent = '--';
    quotaTotalText.textContent = '--';
    quotaProgressBar.style.width = '0%';
    quotaProgressBar.setAttribute('aria-valuenow', '0');
    quotaProgressBar.setAttribute('aria-valuetext', message);
}

function applyQuotaUsageTone(usedPercentage) {
    resetQuotaStateClasses();
    if (usedPercentage >= 100) {
        quotaCard.classList.add('state-full');
        quotaStatus.textContent = 'Full';
        return;
    }
    if (usedPercentage >= 90) {
        quotaCard.classList.add('state-danger');
        quotaStatus.textContent = 'Nearly full';
        return;
    }
    if (usedPercentage >= 75) {
        quotaCard.classList.add('state-warning');
        quotaStatus.textContent = 'Warning';
        return;
    }
    quotaStatus.textContent = 'Healthy';
}

function renderQuota(quota) {
    const total = Math.max(1, Number(quota.totalQuotaBytes || 0));
    const remaining = Math.max(0, Math.min(total, Number(quota.remainingStorageBytes || 0)));
    const used = Math.max(0, total - remaining);
    const usedPercentage = Math.max(0, Math.min(100, Math.round((used / total) * 100)));

    quotaUsageText.textContent = `${formatBytes(used)} / ${formatBytes(total)}`;
    quotaPercentText.textContent = `${usedPercentage}%`;
    quotaUsedText.textContent = formatBytes(used);
    quotaRemainingText.textContent = formatBytes(remaining);
    quotaTotalText.textContent = formatBytes(total);

    quotaProgressBar.style.width = '0%';
    requestAnimationFrame(() => {
        quotaProgressBar.style.width = `${usedPercentage}%`;
    });
    quotaProgressBar.setAttribute('aria-valuenow', String(usedPercentage));
    quotaProgressBar.setAttribute('aria-valuetext', `${usedPercentage}% used, ${formatBytes(remaining)} remaining`);

    applyQuotaUsageTone(usedPercentage);
}

async function loadQuota() {
    setQuotaLoading();
    try {
        const response = await apiFetch('/api/users/me/quota');
        if (!response.ok) throw new Error('Failed to load quota');

        const quotaData = await response.json();
        renderQuota(quotaData);
    } catch (err) {
        if (err.message !== 'Authentication failed' && err.message !== 'No authentication token') {
            setQuotaUnavailable('Unable to load');
        }
    }
}

function applyClientFilteringAndSorting(files) {
    let result = [...files];
    const selectedType = typeFilter.value;

    if (selectedType !== 'all') {
        result = result.filter(file => classifyType(file.contentType || '') === selectedType);
    }

    switch (sortBy.value) {
        case 'name_asc':
            result.sort((a, b) => (a.originalName || '').localeCompare(b.originalName || ''));
            break;
        case 'name_desc':
            result.sort((a, b) => (b.originalName || '').localeCompare(a.originalName || ''));
            break;
        case 'size_asc':
            result.sort((a, b) => (a.sizeBytes || 0) - (b.sizeBytes || 0));
            break;
        case 'size_desc':
            result.sort((a, b) => (b.sizeBytes || 0) - (a.sizeBytes || 0));
            break;
        case 'date_asc':
            result.sort((a, b) => (a.id || 0) - (b.id || 0));
            break;
        case 'date_desc':
        default:
            result.sort((a, b) => (b.id || 0) - (a.id || 0));
            break;
    }

    return result;
}

/* ===== File Operations ===== */

async function loadFiles() {
    showLoading(true);
    hideMessages();

    try {
        const url =
            `/api/files?search=${encodeURIComponent(searchTerm)}&page=${currentPage}&size=${pageSize}`;

        const response = await apiFetch(url);

        const pageData = await response.json();
        lastPageData = pageData;

        const files = pageData.content || [];
        const viewData = applyClientFilteringAndSorting(files);

        renderFiles(viewData);
        updatePaginationUI(pageData);

    } catch (err) {
        if (err.status !== 401 && err.status !== 403) {
            showError(err.message);
        }
    } finally {
        showLoading(false);
    }
}

function renderFiles(files) {
    galleryEl.innerHTML = '';

    if (!files || files.length === 0) {
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    for (const file of files) {
        const card = document.createElement('div');
        card.className = 'card';

        const media = document.createElement('div');
        media.className = 'media-file-icon';

        const type = classifyType(file.contentType || '');
        let icon = '📄';
        if (type === 'image') icon = '🖼️';
        else if (type === 'video') icon = '🎬';
        else if (type === 'pdf') icon = '📕';
        else if (type === 'archive') icon = '📦';

        media.innerHTML = `<div style="font-size:36px">${icon}</div><small>${file.contentType || 'file'}</small>`;

        const body = document.createElement('div');
        body.className = 'card-body';

        const filename = document.createElement('div');
        filename.className = 'filename';
        filename.textContent = file.originalName || 'Unnamed file';

        const meta = document.createElement('div');
        meta.className = 'meta';
        meta.textContent = `${formatBytes(file.sizeBytes || 0)} • ${type}`;

        const actions = document.createElement('div');
        actions.className = 'card-actions';

        const downloadLink = document.createElement('a');
        downloadLink.href = '#';
        downloadLink.textContent = 'Download';
        downloadLink.addEventListener('click', (event) => {
            event.preventDefault();
            downloadFile(file.id, file.originalName || 'file');
        });

        const deleteLink = document.createElement('a');
        deleteLink.href = '#';
        deleteLink.className = 'delete';
        deleteLink.textContent = 'Delete';
        deleteLink.addEventListener('click', async (event) => {
            event.preventDefault();
            await deleteFile(file.id);
        });

        actions.appendChild(downloadLink);
        actions.appendChild(deleteLink);

        body.appendChild(filename);
        body.appendChild(meta);
        body.appendChild(actions);

        card.appendChild(media);
        card.appendChild(body);

        galleryEl.appendChild(card);
    }
}

async function downloadFile(fileId, fileName) {
    try {
        const response =
            await apiFetch(`/api/files/${fileId}/download`);

        const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = blobUrl;
        a.download = fileName;

        document.body.appendChild(a);
        a.click();
        a.remove();

        URL.revokeObjectURL(blobUrl);

    } catch (err) {
        if (err.status !== 401 && err.status !== 403) {
            showError(err.message);
        }
    }
}

async function deleteFile(id) {
    hideMessages();

    try {
        await apiFetch(`/api/files/${id}`, {
            method: 'DELETE'
        });

        showSuccess('Deleted successfully.');

        if (galleryEl.children.length === 1 && currentPage > 0) {
            currentPage--;
        }

        await loadFiles();

    } catch (err) {
        if (err.status !== 401 && err.status !== 403) {
            showError(err.message);
        }
    }
}

function updatePaginationUI(pageData) {
    const hasAny = (pageData.totalElements ?? 0) > 0;

    if (!hasAny) {
        prevBtn.disabled = true;
        nextBtn.disabled = true;
        pageIndicator.textContent = 'No files';
        return;
    }

    prevBtn.disabled = pageData.first;
    nextBtn.disabled = pageData.last;
    pageIndicator.textContent = `Page ${pageData.number + 1} of ${pageData.totalPages}`;
}

function uploadWithProgress(file) {
    return new Promise((resolve, reject) => {
        const token = getToken();
        if (!token) {
            goToLogin();
            reject(new ApiError('No authentication token', 401));
            return;
        }

        uploadPanel.style.display = 'block';
        uploadFileName.textContent = `Uploading: ${file.name}`;
        progressBar.style.width = '0%';

        const xhr = new XMLHttpRequest();
        activeXhr = xhr;

        xhr.open('POST', '/api/files', true);
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);

        xhr.upload.onprogress = (event) => {
            if (!event.lengthComputable) return;
            const percent = Math.round((event.loaded / event.total) * 100);
            progressBar.style.width = `${percent}%`;
        };

        xhr.onload = () => {
            activeXhr = null;

            if (xhr.status >= 200 && xhr.status < 300) {
                resolve();
            } else {
                const message = getXhrErrorMessage(xhr);

                reject(new ApiError(message, xhr.status));
            }
        };

        xhr.onerror = () => {
            activeXhr = null;
            reject(new ApiError('Network error', 0));
        };

        xhr.onabort = () => {
            activeXhr = null;
            reject(new ApiError('Upload canceled', 0));
        };

        const formData = new FormData();
        formData.append('file', file);
        xhr.send(formData);
    });
}

async function uploadFile(file) {
    hideMessages();

    try {
        await uploadWithProgress(file);
        showSuccess('Uploaded successfully.');

        currentPage = 0;
        searchTerm = '';
        searchInput.value = '';

        await loadFiles();
        await loadQuota();
    } catch (err) {
        if (err.message === 'Upload canceled') {
            showError('Upload canceled.');
        } else {
            showError(err.message);
        }
    } finally {
        uploadPanel.style.display = 'none';
        progressBar.style.width = '0%';
        activeXhr = null;
    }
}

async function deleteFile(id) {
    hideMessages();

    try {
        const response = await apiFetch(`/api/files/${id}`, { method: 'DELETE' });
        if (!response.ok && response.status !== 204) throw new Error('Delete failed');

        showSuccess('Deleted successfully.');

        if (galleryEl.children.length === 1 && currentPage > 0) {
            currentPage--;
        }

        await loadFiles();
        await loadQuota();
    } catch {
        showError('Unable to delete file.');
    }
}

function hasFilesInDrag(event) {
    return Array.from(event.dataTransfer?.types || []).includes('Files');
}

function showDropOverlay() {
    dropZone.classList.remove('hidden');
    dropZone.classList.add('overlay');
}

function hideDropOverlay() {
    dropZone.classList.add('hidden');
    dropZone.classList.remove('overlay');
    dragCounter = 0;
}

/* ===== Event Listeners ===== */

uploadBtn.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', () => {
    const file = fileInput.files?.[0];
    if (!file) return;
    uploadFile(file);
    fileInput.value = '';
});

cancelUploadBtn.addEventListener('click', () => {
    if (activeXhr) activeXhr.abort();
});

/* Global DnD (show only while dragging files) */
window.addEventListener('dragenter', (event) => {
    if (!hasFilesInDrag(event)) return;
    event.preventDefault();
    dragCounter++;
    showDropOverlay();
});

window.addEventListener('dragover', (event) => {
    if (!hasFilesInDrag(event)) return;
    event.preventDefault();
});

window.addEventListener('dragleave', (event) => {
    if (!hasFilesInDrag(event)) return;
    event.preventDefault();
    dragCounter--;
    if (dragCounter <= 0) hideDropOverlay();
});

window.addEventListener('drop', (event) => {
    if (!hasFilesInDrag(event)) return;
    event.preventDefault();

    const file = event.dataTransfer?.files?.[0];
    hideDropOverlay();

    if (file) uploadFile(file);
});

logoutBtn.addEventListener('click', (event) => {
    event.preventDefault();
    goToLogin();
});

let debounceTimer;
searchInput.addEventListener('input', (event) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
        searchTerm = event.target.value.trim();
        currentPage = 0;
        loadFiles();
    }, 300);
});

typeFilter.addEventListener('change', () => {
    if (!lastPageData) return;
    renderFiles(applyClientFilteringAndSorting(lastPageData.content || []));
});

sortBy.addEventListener('change', () => {
    if (!lastPageData) return;
    renderFiles(applyClientFilteringAndSorting(lastPageData.content || []));
});

prevBtn.addEventListener('click', () => {
    if (currentPage > 0) {
        currentPage--;
        loadFiles();
    }
});

nextBtn.addEventListener('click', () => {
    currentPage++;
    loadFiles();
});

/* ===== Initialization ===== */

const token = getToken();
if (!token) {
    goToLogin();
} else {
    usernameEl.textContent = getUsername() || 'User';
    hideDropOverlay();
    loadFiles();
    loadQuota();
}
