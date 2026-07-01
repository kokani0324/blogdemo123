/*
  產地地圖（Leaflet）
  ── 資料來源 ──
  目前 blogdemo123 還沒有農場 API，所以 loadFarms() 會：
    1. 先試打 /farms（你 Day 9 要做的後端）
    2. 打不到（404）或沒資料 → 改用下面的「示範農場 DEMO_FARMS」
  之後後端做好 /farms（每筆要有 lat、lng、name…），地圖就會自動換成真實資料。

  /farms 期望的每筆欄位（後端做的時候對齊這些其中一種命名都可以）：
    lat / lng（或 locLat / locLong）、name（或 farmName）、
    address（或 farmAddress）、description（或 farmDesc）、image、id
*/

let farms = [];
const markers = new Map();

const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";

// 後端還沒做時，先用這幾筆讓地圖有東西可看（之後 /farms 有資料就會蓋掉）
const DEMO_FARMS = [
    { id: 1, name: "宜蘭三星青蔥農場", subtitle: "宜蘭縣三星鄉", description: "三星蔥的故鄉，提供採蔥與蔥油餅手作體驗。", lat: 24.6586, lng: 121.6536 },
    { id: 2, name: "台中新社香菇園",   subtitle: "台中市新社區", description: "椴木香菇栽培，可參觀菇寮與採香菇。", lat: 24.2249, lng: 120.8090 },
    { id: 3, name: "嘉義阿里山茶園",   subtitle: "嘉義縣番路鄉", description: "高山烏龍茶產區，採茶與製茶導覽。", lat: 23.4690, lng: 120.6560 },
    { id: 4, name: "花蓮玉里稻米農場", subtitle: "花蓮縣玉里鎮", description: "縱谷有機稻作，插秧與割稻飯體驗。", lat: 23.3340, lng: 121.3160 },
    { id: 5, name: "屏東內埔可可園",   subtitle: "屏東縣內埔鄉", description: "台灣可可從種植到巧克力 tree-to-bar。", lat: 22.6110, lng: 120.5670 }
];

const farmIcon = L.divIcon({
    className: "farm-marker-shell",
    html: `
        <div style="
            width: 42px;
            height: 42px;
            display: grid;
            place-items: center;
            border: 3px solid #4f9368;
            border-radius: 50%;
            background: #fff;
            color: #4f9368;
            box-shadow: 0 4px 12px rgba(0, 0, 0, .28);
        ">
            <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
                <g fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M7 15h18l-2.3 9.5a3 3 0 0 1-2.9 2.3h-7.6a3 3 0 0 1-2.9-2.3L7 15Z"/>
                    <path d="M10 15c.8-3.9 3-6 6-6s5.2 2.1 6 6"/>
                    <path d="M16 9V4"/>
                    <path d="M16 8c-3.1-.2-5.3-1.7-6.5-4.3"/>
                    <path d="M16 8c3.1-.2 5.3-1.7 6.5-4.3"/>
                    <path d="M11 20h10"/>
                </g>
            </svg>
        </div>
    `,
    iconSize: [42, 42],
    iconAnchor: [21, 21],
    popupAnchor: [0, -24]
});

const map = L.map("map", {
    center: [23.7, 121],
    zoom: 8.7,
    minZoom: 7,
    maxBounds: [
        [21.7, 119.0],
        [25.5, 122.8]
    ]
});

L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors"
}).addTo(map);

init();

async function init() {
    farms = await loadFarms();
    renderFarmList();
    renderMarkers();
    // fitFarmBounds();
}

async function loadFarms() {
    try {
        const res = await fetch("/farms");           // Day 9 的後端做好就會走這裡
        if (!res.ok) throw new Error("HTTP " + res.status);
        const data = await res.json();
        const list = Array.isArray(data) ? data : (data.results || data.content || []);
        const normalized = list
            .map(normalizeFarm)
            .filter((f) => hasCoordinate(f.lat) && hasCoordinate(f.lng));
        if (normalized.length) return normalized;     // 有真實資料就用
    } catch (e) {
        console.warn("/farms 尚未提供，改用示範農場：", e.message);
    }
    // 後端還沒做 → 用內建示範資料
    return DEMO_FARMS.map(normalizeFarm);
}

// 把後端可能的不同欄位名統一成地圖要用的格式
function normalizeFarm(f) {
    return {
        id: f.id ?? f.farmId ?? f.farmerId,
        name: f.name || f.farmName || f.farmerName || "未命名農場",
        subtitle: f.subtitle || f.address || f.farmAddress || "台灣在地小農",
        description: f.description || f.farmDesc || "這位小農尚未填寫介紹。",
        lat: Number(f.lat ?? f.locLat),
        lng: Number(f.lng ?? f.locLong),
        image: f.image || f.imageUrl || DEFAULT_IMAGE,
        link: f.id != null ? `producer-detail.html?id=${f.id}` : "#"
    };
}

function hasCoordinate(value) {
    return value !== null
        && value !== undefined
        && String(value).trim() !== ""
        && Number.isFinite(Number(value));
}

function fitFarmBounds() {
    if (!farms.length) return;

    const bounds = L.latLngBounds(
        farms.map((farm) => [farm.lat, farm.lng])
    );

    map.fitBounds(bounds, {
        padding: [80, 80],
        maxZoom: 9
    });
}

function renderFarmList() {
    const list = document.getElementById("farmList");

    list.innerHTML = farms.map((farm) => `
        <article class="farm-card" data-farm-id="${farm.id}">
            <img src="${farm.image}" alt="${farm.name}">
            <div>
                <h2>${farm.name}</h2>
                <p>${farm.subtitle}</p>
            </div>
        </article>
    `).join("");

    list.addEventListener("click", (event) => {
        const card = event.target.closest(".farm-card");
        if (!card) return;

        const farm = farms.find((item) => String(item.id) === card.dataset.farmId);
        if (!farm) return;

        selectFarm(farm);
    });
}

function renderMarkers() {
    farms.forEach((farm) => {
        const marker = L.marker([farm.lat, farm.lng], { icon: farmIcon })
            .addTo(map)
            .bindPopup(farm.name);

        marker.on("click", () => {
            selectFarm(farm);
        });

        markers.set(farm.id, marker);
    });
}

function selectFarm(farm) {
    document.querySelectorAll(".farm-card").forEach((card) => {
        card.classList.toggle("is-active", card.dataset.farmId === String(farm.id));
    });

    map.flyTo([farm.lat, farm.lng], 11);

    const marker = markers.get(farm.id);
    if (marker) marker.openPopup();

    renderFarmDetail(farm);
}

function renderFarmDetail(farm) {
    const detail = document.getElementById("farmDetail");

    detail.classList.remove("is-hidden");
    detail.innerHTML = `
        <button class="detail-close" type="button" aria-label="關閉">×</button>
        <img src="${farm.image}" alt="${farm.name}">
        <div class="farm-detail-content">
            <h2>${farm.name}</h2>
            <p>${farm.description}</p>
            <a class="detail-button" href="${farm.link}">前往農場頁</a>
        </div>
    `;

    detail.querySelector(".detail-close").addEventListener("click", () => {
        detail.classList.add("is-hidden");
    });
}
