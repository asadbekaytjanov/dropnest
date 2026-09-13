const registerForm = document.getElementById('registerForm');
const errorMessage = document.getElementById('errorMessage');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');

function showError(message) {
  errorMessage.textContent = message;
  errorMessage.style.display = 'flex';
}
function hideError() {
  errorMessage.style.display = 'none';
}

// Telegram-like username validation
function validateUsername(username) {
  const u = username.trim();

  if (u.length < 5 || u.length > 32) return 'Username must be 5–32 characters.';
  if (!/^[A-Za-z]/.test(u)) return 'Username must start with a letter.';
  if (!/^[A-Za-z0-9_]+$/.test(u)) return 'Use only letters, numbers, and underscore.';
  if (u.endsWith('_')) return 'Username cannot end with underscore.';
  if (u.includes('__')) return 'Username cannot contain consecutive underscores.';

  return null;
}

// Modern password validation (only shown when invalid submit)
function validatePassword(password) {
  if (password.length < 8) return 'Password must be at least 8 characters.';
  if (!/[A-Z]/.test(password)) return 'Password must include at least one uppercase letter.';
  if (!/[a-z]/.test(password)) return 'Password must include at least one lowercase letter.';
  if (!/[0-9]/.test(password)) return 'Password must include at least one number.';
  if (!/[^A-Za-z0-9]/.test(password)) return 'Password must include at least one special character.';
  return null;
}

usernameInput.addEventListener('input', hideError);
passwordInput.addEventListener('input', hideError);

registerForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  hideError();

  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  const usernameError = validateUsername(username);
  if (usernameError) {
    showError(usernameError);
    usernameInput.focus();
    return;
  }

  const passwordError = validatePassword(password);
  if (passwordError) {
    showError(passwordError);
    passwordInput.focus();
    return;
  }

  const submitButton = registerForm.querySelector('button[type="submit"]');
  const originalText = submitButton.textContent;
  submitButton.disabled = true;
  submitButton.textContent = 'Registering...';

  try {
    const params = new URLSearchParams();
    params.set('username', username);
    params.set('password', password);

    const response = await fetch('/api/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params
    });

    if (response.ok) {
      window.location.href = 'login.html';
      return;
    }

    if (response.status === 409) {
      const data = await response.json().catch(() => ({}));
      showError(data.message || 'Username is already taken.');
    } else {
      showError('Something went wrong. Please try again.');
    }
  } catch {
    showError('Unable to reach server. Please try again.');
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = originalText;
  }
});