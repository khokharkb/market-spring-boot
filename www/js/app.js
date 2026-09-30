// ===== CONFIGURATION =====
// IMPORTANT: Utilisez votre IP
const API_BASE_URL = 'http://192.168.100.8:8080';
const MOBILE_API_URL = `${API_BASE_URL}/api/mobile`;

console.log('🚀 Configuration loaded. API URL:', API_BASE_URL);

// ===== FONCTION DE TEST =====
async function testConnection() {
    console.log('🔍 Testing connection to Spring Boot...');

    try {
        // Test 1: Ping simple
        const ping = await fetch(`${API_BASE_URL}/ping`);
        console.log('✅ Ping:', await ping.text());

        // Test 2: API mobile
        const apiTest = await fetch(`${MOBILE_API_URL}/test`);
        const apiData = await apiTest.json();
        console.log('✅ API Mobile:', apiData);

        // Test 3: Health check
        const health = await fetch(`${MOBILE_API_URL}/health`);
        console.log('✅ Health:', await health.text());

        return true;
    } catch (error) {
        console.error('❌ Connection failed:', error);
        console.log('💡 Tips:');
        console.log('1. Is Spring Boot running?');
        console.log('2. Check IP address:', API_BASE_URL);
        console.log('3. Check if port 8080 is open');
        return false;
    }
}

// ===== FONCTIONS AUTH =====
async function submitLogin(event) {
    if (event) event.preventDefault();

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    console.log('🔐 Login attempt:', email);

    try {
        const response = await fetch(`${MOBILE_API_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();
        console.log('📥 Login response:', data);

        if (data.success) {
            localStorage.setItem('market_user', JSON.stringify(data.user));
            alert('✅ Connexion réussie !');
            closeLogin();
            location.reload();
        } else {
            alert('❌ ' + data.error);
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Erreur réseau. Vérifiez la console.');
    }
}

async function submitRegister(event) {
    if (event) event.preventDefault();

    const name = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const confirm = document.getElementById('registerConfirm').value;

    if (password !== confirm) {
        alert('Les mots de passe ne correspondent pas');
        return;
    }

    console.log('📝 Register attempt:', email);

    try {
        const response = await fetch(`${MOBILE_API_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name, email, password })
        });

        const data = await response.json();
        console.log('📥 Register response:', data);

        if (data.success) {
            alert('✅ Inscription réussie !');
            showLoginTab('login');
            document.getElementById('loginEmail').value = email;
        } else {
            alert('❌ ' + data.error);
        }
    } catch (error) {
        console.error('Register error:', error);
        alert('Erreur réseau');
    }
}

// ===== INITIALISATION =====
document.addEventListener('DOMContentLoaded', function() {
    console.log('📱 DOM loaded. Initializing...');

    // Tester la connexion immédiatement
    testConnection().then(connected => {
        if (!connected) {
            console.log('⚠️ Backend not available. Using simulation mode.');

            // Ajouter un utilisateur de test
            localStorage.setItem('market_mock_users', JSON.stringify([
                { id: 1, name: "Test", email: "test@market.com", password: "test123" }
            ]));

            alert('⚠️ Mode simulation activé. Utilisez:\nEmail: test@market.com\nMot de passe: test123');
        }
    });

    // Configurer les événements
    setupEventListeners();

    // Ajouter le bouton de test
    addTestButton();
});

function setupEventListeners() {
    // Formulaire login
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', submitLogin);
    }

    // Formulaire register
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', submitRegister);
    }

    // Bouton de fermeture
    const closeBtn = document.querySelector('.close-btn');
    if (closeBtn) {
        closeBtn.addEventListener('click', function() {
            document.getElementById('loginOverlay').style.display = 'none';
        });
    }

    // Onglets login/register
    document.querySelectorAll('.tab-btn').forEach((btn, index) => {
        btn.addEventListener('click', function() {
            const tab = index === 0 ? 'login' : 'register';
            showLoginTab(tab);
        });
    });
}

function addTestButton() {
    const testBtn = document.createElement('button');
    testBtn.innerHTML = '🔧 TEST SPRING';
    testBtn.style.cssText = `
        position: fixed;
        top: 10px;
        right: 10px;
        z-index: 99999;
        background: #28a745;
        color: white;
        padding: 10px 15px;
        border: none;
        border-radius: 5px;
        font-weight: bold;
        cursor: pointer;
    `;

    testBtn.onclick = function() {
        console.clear();
        console.log('=== TEST SPRING BOOT ===');
        testConnection();
    };

    document.body.appendChild(testBtn);
}

// ===== EXPORT DES FONCTIONS GLOBALES =====
window.submitLogin = submitLogin;
window.submitRegister = submitRegister;
window.testConnection = testConnection;