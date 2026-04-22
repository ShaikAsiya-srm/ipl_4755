const teamLogos = {
    "Gujarat Titans": "assets/logos/gt.png",
    "Royal Challengers Bangalore": "assets/logos/rcb.png",
    "Punjab Kings": "assets/logos/pbks.png",
    "Mumbai Indians": "assets/logos/mi.png",
    "Kolkata Knight Riders": "assets/logos/kkr.png",
    "Delhi Capitals": "assets/logos/dc.png",
    "Sunrisers Hyderabad": "assets/logos/srh.png",
    "Rajasthan Royals": "assets/logos/rr.png",
    "Chennai Super Kings": "assets/logos/csk.png",
    "Lucknow Super Giants": "assets/logos/lsg.png"
};

function getTeamLogo(teamName) {
    return teamLogos[teamName] || null;
}

function teamShortName(name) {
    const map = {
        "Royal Challengers Bangalore": "RCB",
        "Chennai Super Kings": "CSK",
        "Mumbai Indians": "MI",
        "Kolkata Knight Riders": "KKR",
        "Rajasthan Royals": "RR",
        "Sunrisers Hyderabad": "SRH",
        "Punjab Kings": "PBKS",
        "Gujarat Titans": "GT",
        "Lucknow Super Giants": "LSG",
        "Delhi Capitals": "DC",
    };
    return map[name] || name?.slice(0, 3).toUpperCase() || "?";
}

function teamColor(name) {
    const colors = {
        "Royal Challengers Bangalore": "#d32f2f",
        "Chennai Super Kings": "#fbc02d",
        "Mumbai Indians": "#1976d2",
        "Kolkata Knight Riders": "#7b1fa2",
        "Rajasthan Royals": "#ec407a",
        "Sunrisers Hyderabad": "#f57c00",
        "Punjab Kings": "#e53935",
        "Gujarat Titans": "#1e88e5",
        "Lucknow Super Giants": "#43a047",
        "Delhi Capitals": "#1565c0",
    };
    return colors[name] || "#555";
}

window.teamLogos = teamLogos;
window.getTeamLogo = getTeamLogo;
window.teamShortName = teamShortName;
window.teamColor = teamColor;