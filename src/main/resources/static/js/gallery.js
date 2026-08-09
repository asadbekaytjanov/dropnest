const usernameEl = document.getElementById('username');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const emptyState = document.getElementById('emptyState');
const galleryEl = document.getElementById('gallery');
const fileInput = document.getElementById('fileInput');
const logoutBtn = document.getElementById('logoutBtn');

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
    successMessage.style.display = 'none';
}

function showSuccess(message) {
    successMessage.textContent = message;
    successMessage.style.display = 'block';
    errorMessage.style.display = 'none';
}

function hideMessages() {
    errorMessage.style.display = 'none';
    successMessage.style.display = 'none';
}

function goToLogin() {
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    window.location.href = 'login.html';
}

function renderPhotos(photos) {
    galleryEl.innerHTML = '';

    if (!photos || photos.length === 0) {
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    for (const photo of photos) {
        const card = document.createElement('div');
        card.className = 'card';

        let media;
        if (photo.contentType && photo.contentType.startsWith('video/')) {
            media = document.createElement('video');
            media.className = 'media-video';
            media.src = `/api/photos/${photo.id}`;
            media.muted = true;
            media.preload = 'metadata';
        } else {
            media = document.createElement('img');
            media.className = 'media-img';
            media.src = `/api/photos/${photo.id}`;
            media.alt = photo.fileName;
        }

        const mediaLink = document.createElement('a');
        mediaLink.href = `/api/photos/${photo.id}`;
        mediaLink.target = '_blank';
        mediaLink.rel = 'noopener noreferrer';
        mediaLink.style.display = 'block';
        mediaLink.appendChild(media);
        card.appendChild(mediaLink);

        const body = document.createElement('div');
        body.className = 'card-body';

        const filename = document.createElement('div');
        filename.className = 'filename';
        filename.textContent = photo.fileName;
        body.appendChild(filename);

        const actions = document.createElement('div');
        actions.className = 'card-actions';

        const viewLink = document.createElement('a');
        viewLink.href = `/api/photos/${photo.id}`;
        viewLink.target = '_blank';
        viewLink.rel = 'noopener noreferrer';
        viewLink.textContent = 'View';
        actions.appendChild(viewLink);

        const downloadLink = document.createElement('a');
        downloadLink.href = `/api/photos/${photo.id}/download`;
        downloadLink.textContent = 'Download';
        actions.appendChild(downloadLink);

        const deleteLink = document.createElement('a');
        deleteLink.className = 'delete';
        deleteLink.textContent = 'Delete';
        deleteLink.addEventListener('click', (event) => {
            event.preventDefault();
            deletePhoto(photo.id);
        });
        actions.appendChild(deleteLink);

        body.appendChild(actions);
        card.appendChild(body);
        galleryEl.appendChild(card);
    }
}

async function loadPhotos() {
    try {
        const response = await fetch('/api/photos');
        if (response.status === 401) {
            goToLogin();
            return;
        }

        if (!response.ok) {
            const errData = await response.json();
            showError(errData.message)
            return;
        }

        const photos = await response.json();
        renderPhotos(photos);
    } catch (err) {
        showError('Unable to reach the server. Please try again.');
    }
}

async function uploadFile(file) {
    hideMessages();

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/photos', {
            method: 'POST',
            body: formData
        });

        if (response.status === 401) {
            goToLogin();
            return;
        }

        if (!response.ok) {
            const errData = await response.json();
            showError(errData.message)
            return;
        }

        showSuccess('Uploaded successfully.');
        await loadPhotos();
    } catch (err) {
        showError('Unable to reach the server. Please try again.');
    }
}

async function deletePhoto(id) {
    hideMessages();

    try {
        const response = await fetch(`/api/photos/${id}`, { method: 'DELETE' });

        if (response.status === 401) {
            goToLogin();
            return;
        }

        if (!response.ok && response.status !== 204) {
            showError('Delete failed. Please try again.');
            return;
        }

        showSuccess('Deleted successfully.');
        await loadPhotos();
    } catch (err) {
        showError('Unable to reach the server. Please try again.');
    }
}

fileInput.addEventListener('change', () => {
    const file = fileInput.files[0];
    if (file) {
        uploadFile(file)
        fileInput.value = '';
    }
});

logoutBtn.addEventListener('click', async (event) => {
    event.preventDefault();
    try {
        await fetch('/api/logout', { method: 'POST' });
    } catch (err) {
        // ignore network errors on logout, still clear client state
    }
    goToLogin();
});

const storedUsername = localStorage.getItem('username');
const storedUserId = localStorage.getItem('userId')
if (!storedUsername || !storedUserId) {
   goToLogin()

} else{
    usernameEl.textContent = storedUsername;
    loadPhotos()
}