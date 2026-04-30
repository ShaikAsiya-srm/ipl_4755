// =============================================================================
// IPL-FRONTEND/app.js (FULL FIXED VERSION)
// =============================================================================

/* ---------------------------------------------------------------------------
   Global Variables
--------------------------------------------------------------------------- */
var currentUser = null;
var currentMatch = null;
var allMatches = [];

/* ---------------------------------------------------------------------------
   API CONFIG (FIXED)
--------------------------------------------------------------------------- */
var API_BASE = "http://localhost:8081/ipl-backend/api";

/* ---------------------------------------------------------------------------
   DOM Helpers
--------------------------------------------------------------------------- */
function $(selector) {
    return document.querySelector(selector);
}

function $$(selector) {
    return Array.from(document.querySelectorAll(selector));
}

/* ---------------------------------------------------------------------------
   ERROR HANDLER
--------------------------------------------------------------------------- */
function showError(message, containerId) {
    console.error('[IPL ERROR]', message);

    var container = containerId ? $(containerId) : null;
    if (container) {
        container.innerHTML =
            '<div class="alert alert-error">' + message + '</div>';
    }
}

/* ---------------------------------------------------------------------------
   LOADING TOGGLE
--------------------------------------------------------------------------- */
function toggleLoading(element, isLoading) {
    if (!element) return;

    if (isLoading) {
        element.disabled = true;
        element.dataset.originalText = element.innerHTML;
        element.innerHTML = "Loading...";
    } else {
        element.disabled = false;
        if (element.dataset.originalText) {
            element.innerHTML = element.dataset.originalText;
        }
    }
}

/* ---------------------------------------------------------------------------
   API FETCH (SAFE VERSION)
--------------------------------------------------------------------------- */
function apiFetch(endpoint, options) {
    var url = endpoint.startsWith("http")
        ? endpoint
        : API_BASE + endpoint;

    return fetch(url, Object.assign({
        headers: {
            "Content-Type": "application/json"
        }
    }, options))
    .then(async function (res) {

        if (!res.ok) {
            var text = await res.text();
            throw new Error(text || "HTTP Error " + res.status);
        }

        // handle empty response safely
        var text = await res.text();
        try {
            return JSON.parse(text);
        } catch (e) {
            return text;
        }
    });
}

/* ---------------------------------------------------------------------------
   DATE FORMAT
--------------------------------------------------------------------------- */
function parseMatchDate(dateStr) {
    if (!dateStr) return "TBD";

    var d = new Date(dateStr);
    if (isNaN(d.getTime())) return "Invalid Date";

    return d.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        hour12: true
    });
}

/* ---------------------------------------------------------------------------
   MATCH STATUS
--------------------------------------------------------------------------- */
function getMatchStatusMarkup(status) {
    if (!status) return '<span class="badge">scheduled</span>';

    return '<span class="badge badge-' + status + '">' + status + '</span>';
}

/* ---------------------------------------------------------------------------
   SYNC MATCHES
--------------------------------------------------------------------------- */
function syncMatches() {
    fetch(API_BASE + "/matches/sync")
        .then(function (res) {
            return res.json();
        })
        .then(function (data) {
            alert(data.message || "Sync completed");
        })
        .catch(function (err) {
            console.error(err);
            alert("Sync failed: " + err.message);
        });
}

/* ---------------------------------------------------------------------------
   LOAD TODAY MATCH
--------------------------------------------------------------------------- */
async function loadToday() {
    var card = $("#today-match-card");
    if (!card) return;

    toggleLoading($("#btn-refresh-today"), true);
    card.innerHTML = "Loading...";

    try {
        var data = await apiFetch("/matches/today");
        currentMatch = data;
        renderTodayCard(data);
    } catch (err) {
        showError("Failed to load today's match", "#today-match-card");
    } finally {
        toggleLoading($("#btn-refresh-today"), false);
    }
}

/* ---------------------------------------------------------------------------
   RENDER TODAY MATCH
--------------------------------------------------------------------------- */
function renderTodayCard(match) {
    var card = $("#today-match-card");
    if (!card) return;

    if (!match) {
        card.innerHTML = "<p>No match today</p>";
        return;
    }

    card.innerHTML = `
        <div class="match-card-header">
            <h3>${match.team1} vs ${match.team2}</h3>
            ${getMatchStatusMarkup(match.status)}
        </div>
        <div class="match-card-body">
            <p>${parseMatchDate(match.date)}</p>
            <p>${match.stadium || ""}</p>
        </div>
    `;
}

/* ---------------------------------------------------------------------------
   LOAD MATCHES
--------------------------------------------------------------------------- */
async function loadMatches() {
    var table = $("#matchList");
    if (!table) return;

    var tbody = table.querySelector("tbody") || table;
    tbody.innerHTML = "<tr><td>Loading...</td></tr>";

    try {
        var matches = await apiFetch("/matches");
        allMatches = matches || [];
        renderMatchesTable(allMatches, tbody);
    } catch (err) {
        showError("Failed to load matches", "#matchList");
    }
}

/* ---------------------------------------------------------------------------
   RENDER TABLE
--------------------------------------------------------------------------- */
function renderMatchesTable(matches, tbody) {
    if (!matches || matches.length === 0) {
        tbody.innerHTML = "<tr><td>No matches found</td></tr>";
        return;
    }

    tbody.innerHTML = matches.map(function (m) {
        return `
            <tr>
                <td>${m.team1} vs ${m.team2}</td>
                <td>${parseMatchDate(m.date)}</td>
                <td>${m.stadium || ""}</td>
                <td>${getMatchStatusMarkup(m.status)}</td>
            </tr>
        `;
    }).join("");
}

/* ---------------------------------------------------------------------------
   INIT APP
--------------------------------------------------------------------------- */
async function init() {
    console.log("IPL App Started");

    try {
        await loadToday();
        await loadMatches();
    } catch (err) {
        console.error("Init error:", err);
    }
}

/* ---------------------------------------------------------------------------
   START
--------------------------------------------------------------------------- */
document.addEventListener("DOMContentLoaded", init);
