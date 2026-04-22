const API_BASE = "http://localhost:8081/ipl-backend/api";

let currentUser = null;
let currentMatch = null;
let allMatches = [];

// ---------------- SESSION ----------------
async function checkSession() {
    try {
        const response = await fetch(`${API_BASE}/auth`, {
            credentials: "include"
        });
        if (response.ok) {
            currentUser = await response.json();
            updateUserUI(currentUser);
        } else {
            // Redirect to login if on dashboard and not authenticated
            if (window.location.pathname === '/dashboard') {
                window.location.href = '/';
            }
        }
    } catch (error) {
        console.error("Session check error:", error);
    }
}

// ---------------- UI ----------------
function updateUserUI(user) {
    const nav = document.querySelector('.nav-buttons');
    if (user) {
        nav.innerHTML = `
            <button class="nav-btn" onclick="showSection('predict')">Home</button>
            <button class="nav-btn" onclick="showSection('matches')">Matches</button>
            <button class="nav-btn" onclick="showSection('leaderboard')">Leaderboard</button>
            <span class="nav-user">${user.username} (${user.score || 0} pts)</span>
            <button class="nav-btn" onclick="logout()">Logout</button>
        `;
    } else {
        nav.innerHTML = `
            <button class="nav-btn" onclick="showSection('predict')">Home</button>
            <button class="nav-btn" onclick="showSection('matches')">Matches</button>
            <button class="nav-btn" onclick="showSection('leaderboard')">Leaderboard</button>
            <button class="nav-btn" onclick="openLogin()">Login</button>
        `;
    }
}

// ---------------- MATCHES ----------------
const FALLBACK_MATCHES = [
    { id: 1, team1: 'Royal Challengers Bangalore', team2: 'Kolkata Knight Riders', stadium: 'M. Chinnaswamy Stadium', time: '3:30 PM', date: '2026-04-20' },
    { id: 2, team1: 'Gujarat Titans', team2: 'Sunrisers Hyderabad', stadium: 'Narendra Modi Stadium', time: '7:30 PM', date: '2026-04-20' },
    { id: 3, team1: 'Delhi Capitals', team2: 'Mumbai Indians', stadium: 'Arun Jaitley Stadium', time: '7:30 PM', date: '2026-04-21' }
];

const TEAMS = [
    'Royal Challengers Bangalore', 'Kolkata Knight Riders', 'Gujarat Titans', 
    'Sunrisers Hyderabad', 'Delhi Capitals', 'Mumbai Indians', 
    'Chennai Super Kings', 'Punjab Kings', 'Rajasthan Royals', 'Lucknow Super Giants'
];

function populateTeamDropdowns() {
    const dropdownIds = ['predictionWinner', 'predictionToss', 'predictionBatFirst',
                          'modalPredictionWinner', 'modalPredictionToss', 'modalPredictionBatFirst'];

    dropdownIds.forEach(id => {
        const select = document.getElementById(id);
        if (select) {
            // Keep first option, add teams
            TEAMS.forEach(team => {
                const option = document.createElement('option');
                option.value = team;
                option.textContent = team;
                select.appendChild(option);
            });
        }
    });
}

function populateCurrentMatchDropdowns(team1, team2) {
    const dropdownIds = ['toss', 'bat'];

    dropdownIds.forEach(id => {
        const select = document.getElementById(id);
        if (select) {
            // Clear existing options except first
            while (select.options.length > 1) {
                select.remove(1);
            }
            // Add current match teams
            [team1, team2].forEach(team => {
                const option = document.createElement('option');
                option.value = team;
                option.textContent = team;
                select.appendChild(option);
            });
        }
    });
}

async function loadToday() {
    try {
        const res = await fetch(`${API_BASE}/matches/today`);
        if (res.ok) {
            const match = await res.json();
            if (match && match.team1) {
                currentMatch = match;
                renderTodayMatch(match);
                return;
            }
        }
    } catch (error) {
        console.warn("Backend unavailable, using fallback data:", error);
    }
    
    // Use fallback data
    const todayMatch = FALLBACK_MATCHES[0];
    currentMatch = todayMatch;
    renderTodayMatch(todayMatch);
}

function renderTodayMatch(match) {
    if (!match || !match.team1) {
        document.getElementById('matchPreviewTitle').textContent = 'No Match Scheduled Today';
        return;
    }

    // Update Hero elements
    const previewTitle = document.getElementById('matchPreviewTitle');
    const detailLine = document.getElementById('matchDetailLine');
    const dateTimeLine = document.getElementById('matchDateTimeLine');

    if (previewTitle) previewTitle.textContent = `${match.team1} vs ${match.team2} Preview`;
    if (detailLine) detailLine.textContent = `${match.id || 'Live'} Match, ${match.stadium || 'Venue TBD'}`;
    if (dateTimeLine) dateTimeLine.textContent = `${match.date || 'Today'}, ${match.time || '7:30 PM'}`;

    // Update team 1
    const team1Logo = document.getElementById('team1Logo');
    const team1Name = document.getElementById('team1Name');
    if (team1Logo) team1Logo.src = match.logo1 || (window.teamLogos ? window.teamLogos[match.team1] : '') || 'https://www.iplt20.com/assets/images/ipl-logo-new-old.png';
    if (team1Name) team1Name.textContent = match.team1;

    // Update team 2
    const team2Logo = document.getElementById('team2Logo');
    const team2Name = document.getElementById('team2Name');
    if (team2Logo) team2Logo.src = match.logo2 || (window.teamLogos ? window.teamLogos[match.team2] : '') || 'https://www.iplt20.com/assets/images/ipl-logo-new-old.png';
    if (team2Name) team2Name.textContent = match.team2;

    // Start Countdown
    startCountdown(match.date, match.time);

    // Populate prediction dropdowns with current match teams
    populateCurrentMatchDropdowns(match.team1, match.team2);
}

function startCountdown(matchDate, matchTime) {
    // Assuming 2026 format if not provided
    const target = new Date(`${matchDate || '2026-04-23'}T${matchTime ? matchTime.replace('PM', '').trim() : '19:30'}:00`);
    
    const update = () => {
        const now = new Date();
        const diff = target - now;

        if (diff <= 0) {
            document.querySelectorAll('.countdown-value').forEach(v => v.textContent = '00');
            return;
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const secs = Math.floor((diff % (1000 * 60)) / 1000);

        const cdDays = document.getElementById('cd-days');
        const cdHours = document.getElementById('cd-hours');
        const cdMins = document.getElementById('cd-mins');
        const cdSecs = document.getElementById('cd-secs');

        if (cdDays) cdDays.textContent = days.toString().padStart(2, '0');
        if (cdHours) cdHours.textContent = hours.toString().padStart(2, '0');
        if (cdMins) cdMins.textContent = mins.toString().padStart(2, '0');
        if (cdSecs) cdSecs.textContent = secs.toString().padStart(2, '0');
    };

    update();
    setInterval(update, 1000);
}

async function loadMatches() {
    try {
        const res = await fetch(`${API_BASE}/matches`);
        if (res.ok) {
            const matches = await res.json();
            if (matches && matches.length > 0) {
                allMatches = matches;
                renderMatches(matches);
                return;
            }
        }
    } catch (error) {
        console.warn("Backend unavailable, using fallback matches:", error);
    }
    
    // Use fallback data
    allMatches = FALLBACK_MATCHES;
    renderMatches(FALLBACK_MATCHES);
}

function renderMatches(matches) {
    let output = "";

    matches.forEach(m => {
        output += `
            <tr>
                <td>${m.date || "TBD"}</td>
                <td>${m.team1} vs ${m.team2}</td>
                <td>${m.stadium || "Not available"}</td>
                <td><span class="badge">${m.time || 'TBD'}</span></td>
            </tr>
        `;
    });

    document.getElementById("matchList").innerHTML = output;
}

async function loadLeaderboard() {
    try {
        const response = await fetch(`${API_BASE}/leaderboard`);
        if (response.ok) {
            const users = await response.json();
            renderLeaderboard(users);
        } else {
            console.error('Failed to load leaderboard:', response.status);
            // Show fallback data
            renderLeaderboard([]);
        }
    } catch (error) {
        console.error('Error loading leaderboard:', error);
        // Show fallback data
        renderLeaderboard([]);
    }
}

function renderLeaderboard(users) {
    const tbody = document.querySelector('#leaderboardTable tbody');
    if (!tbody) return;

    let output = '';

    if (users && users.length > 0) {
        users.forEach((user, index) => {
            const rank = index + 1;
            const medal = rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : '';
            const accuracy = Math.floor(Math.random() * 30) + 70; // Mock accuracy
            const totalPredictions = Math.floor(user.score / 20) + Math.floor(Math.random() * 10);
            
            output += `
                <tr>
                    <td>${rank}</td>
                    <td>${medal} ${user.username}</td>
                    <td>${totalPredictions}</td>
                    <td>${accuracy}%</td>
                    <td><strong>${user.score}</strong></td>
                </tr>
            `;
        });
    } else {
        // Fallback static data
        output = `
            <tr><td>1</td><td>🥇 CricketMaster</td><td>45</td><td>84%</td><td><strong>2,850</strong></td></tr>
            <tr><td>2</td><td>🥈 PredictPro</td><td>45</td><td>80%</td><td><strong>2,680</strong></td></tr>
            <tr><td>3</td><td>🥉 AnalystKing</td><td>45</td><td>77%</td><td><strong>2,550</strong></td></tr>
        `;
    }

    tbody.innerHTML = output;
}

// ---------------- LOGOUT ----------------
async function logout() {
    try {
        await fetch(`${API_BASE}/auth`, {
            method: "DELETE",
            credentials: "include"
        });
    } catch (error) {
        console.error("Logout error:", error);
    }

    currentUser = null;
    window.location.href = '/';
}

// ---------------- PREDICTION ----------------
async function submitPrediction() {
    if (!currentUser) {
        alert("Login first");
        return;
    }

    const toss = document.getElementById("toss").value;
    const bat = document.getElementById("bat").value;
    const player = document.getElementById("player").value;

    const prediction = {
        userId: currentUser.id,
        matchId: currentMatch.id,
        tossWinner: toss,
        batFirst: bat,
        player: player
    };

    try {
        const response = await fetch(`${API_BASE}/predictions`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(prediction)
        });

        if (response.ok) {
            alert("Prediction submitted");
        } else {
            alert("Error submitting prediction");
        }

    } catch (error) {
        alert("Prediction error: " + error.message);
    }
}

// ---------------- UI POPUPS ----------------
function openLoginModal() {
    document.getElementById("loginModal").classList.add("active");
    document.getElementById("registerModal").classList.remove("active");
}

function closeLoginModal() {
    document.getElementById("loginModal").classList.remove("active");
}

function openLogin() {
    openLoginModal();
}

function closeLogin() {
    closeLoginModal();
}

function openRegisterModal() {
    document.getElementById("registerModal").classList.add("active");
    document.getElementById("loginModal").classList.remove("active");
}

function closeRegisterModal() {
    document.getElementById("registerModal").classList.remove("active");
}

function openRegister() {
    openRegisterModal();
}

function closeRegister() {
    closeRegisterModal();
}

function switchToSignup(event) {
    if (event) event.preventDefault();
    openRegisterModal();
}

function switchToLogin(event) {
    if (event) event.preventDefault();
    openLoginModal();
}

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        alert("Enter username and password");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/auth?action=login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (response.ok) {
            const user = await response.json();
            currentUser = user;
            updateUserUI(user);
            closeLoginModal();
            alert("Login successful");
        } else {
            const error = await response.json();
            alert(error.error || "Invalid credentials");
        }

    } catch (error) {
        alert("Login error: " + error.message);
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;

    if (!username || !password) {
        alert("Enter username and password");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/auth?action=register`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (response.ok) {
            alert("Registration successful! Please login.");
            switchToLogin(event);
        } else {
            const error = await response.json();
            alert(error.error || "Registration failed");
        }

    } catch (error) {
        alert("Registration error: " + error.message);
    }
}
function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // Remove active class from all nav buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Show target section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');

        // Load section-specific data
        if (sectionId === 'leaderboard') {
            loadLeaderboard();
        } else if (sectionId === 'matches') {
            loadMatches();
        }
    }

    // Add active class to matching nav button
    document.querySelectorAll('.nav-btn').forEach(btn => {
        const matchesSection = btn.getAttribute('onclick')?.includes(`'${sectionId}'`);
        if (matchesSection) {
            btn.classList.add('active');
        }
    });
}
// ---------------- TEAM MODAL FUNCTIONS ----------------
function openTeamModal(teamType) {
    const modal = document.getElementById('teamModal');
    if (!modal || !window.currentMatchData) return;

    const teamData = teamType === 'team1' ? {
        name: window.currentMatchData.team1,
        logo: window.currentMatchData.logo1
    } : {
        name: window.currentMatchData.team2,
        logo: window.currentMatchData.logo2
    };

    // Get team stats from database
    const teamStats = getTeamStats(teamData.name);

    // Update modal content
    document.getElementById('modalTeamLogo').src = teamData.logo || '';
    document.getElementById('modalTeamName').textContent = teamData.name;
    document.getElementById('modalTeamSubtitle').textContent = `IPL 2026 • ${window.currentMatchData.stadium || 'Venue TBD'}`;

    // Update stats
    document.getElementById('modalMatchesPlayed').textContent = teamStats.played;
    document.getElementById('modalWins').textContent = teamStats.wins;
    document.getElementById('modalLosses').textContent = teamStats.losses;
    document.getElementById('modalPoints').textContent = teamStats.points;

    // Update recent matches
    updateRecentMatches(teamData.name);

    modal.classList.add('active');
}

function closeTeamModal() {
    document.getElementById('teamModal').classList.remove('active');
}

function getTeamStats(teamName) {
    // Mock team stats - in real app, this would come from backend
    const teamStats = {
        'Royal Challengers Bangalore': { played: 11, wins: 7, losses: 4, points: 15 },
        'Kolkata Knight Riders': { played: 11, wins: 6, losses: 5, points: 12 },
        'Gujarat Titans': { played: 12, wins: 8, losses: 3, points: 17 },
        'Sunrisers Hyderabad': { played: 11, wins: 5, losses: 6, points: 10 },
        'Delhi Capitals': { played: 12, wins: 7, losses: 5, points: 14 },
        'Mumbai Indians': { played: 12, wins: 8, losses: 4, points: 16 },
        'Chennai Super Kings': { played: 11, wins: 5, losses: 6, points: 11 },
        'Punjab Kings': { played: 11, wins: 7, losses: 4, points: 14 },
        'Rajasthan Royals': { played: 11, wins: 5, losses: 6, points: 10 },
        'Lucknow Super Giants': { played: 11, wins: 4, losses: 7, points: 8 }
    };

    return teamStats[teamName] || { played: 0, wins: 0, losses: 0, points: 0 };
}

function updateRecentMatches(teamName) {
    // Mock recent matches - in real app, this would come from backend
    const recentResults = ['W', 'L', 'W', 'W', 'L']; // W = Win, L = Loss

    const container = document.getElementById('modalRecentMatches');
    container.innerHTML = '';

    recentResults.forEach(result => {
        const matchDiv = document.createElement('div');
        matchDiv.className = `recent-match ${result.toLowerCase()}`;
        matchDiv.textContent = result;
        container.appendChild(matchDiv);
    });
}

// ---------------- INIT ----------------
async function init() {
    populateTeamDropdowns();

    // Try to sync matches from web scraping
    try {
        const syncRes = await fetch(`${API_BASE}/matches/sync`);
        if (syncRes.ok) {
            console.log("Matches synced successfully");
        }
    } catch (e) {
        console.warn("Sync failed, using existing data:", e);
    }

    // Load today's match and all matches
    await loadToday();
    await loadMatches();

    // Load leaderboard if leaderboard section is active
    const activeSection = document.querySelector('.section.active');
    if (activeSection && activeSection.id === 'leaderboard') {
        await loadLeaderboard();
    }

    checkSession();
}

init();