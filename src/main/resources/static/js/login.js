const loginForm = document.getElementById('loginForm');
const errorMessage = document.getElementById('errorMessage');

function showError(message) {
  errorMessage.textContent = message;
  errorMessage.style.display = 'flex';
}
function hideError() {
  errorMessage.style.display = 'none';
}

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  hideError();

  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  const submitButton = loginForm.querySelector('button[type="submit"]');
  const originalText = submitButton.textContent;
  submitButton.disabled = true;
  submitButton.textContent = 'Logging in...';

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('username', data.username || username);
      window.location.href = 'myfiles.html';
      return;
    }

    if (response.status === 401) {
      showError('Invalid username or password.');
    } else {
      showError('Something went wrong. Please try again.');
    }
  } catch {
    showError('Unable to reach the server. Please try again.');
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = originalText;
  }
});