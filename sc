--   JOMHUB v1 - Sailor Piece

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Workspace = game:GetService("Workspace")
local RunService = game:GetService("RunService")
local HttpService = game:GetService("HttpService")
local VirtualUser = game:GetService("VirtualUser")

if not game:IsLoaded() then
    game.Loaded:Wait()
end
task.wait(4) -- Extra delay to ensure the game environment is fully loaded

local LocalPlayer = Players.LocalPlayer
while not LocalPlayer do task.wait(); LocalPlayer = Players.LocalPlayer end
local UserInputService = game:GetService("UserInputService")
local TweenService = game:GetService("TweenService")
local TextService = game:GetService("TextService")
local request = (syn and syn.request) or (http and http.request) or http_request or (fluxus and fluxus.request) or request

-- // ANTI-DUPLICATION KILL SWITCH //
if _G.JomHubRunning then
    _G.JomHubRunning = false
    task.wait(0.6) 
end
_G.JomHubRunning = true

-- // GAME ENGINE PATHS //
local RemoteEvents = ReplicatedStorage:WaitForChild("RemoteEvents", 5)
local CombatRemote = RemoteEvents and RemoteEvents:FindFirstChild("CombatRemote")
local KatanaCombatRemote = RemoteEvents and RemoteEvents:FindFirstChild("KatanaCombatRemote")
local QuestAccept = RemoteEvents and RemoteEvents:FindFirstChild("QuestAccept")
local QuestComplete = RemoteEvents and RemoteEvents:FindFirstChild("QuestComplete")
local QuestAbandon = RemoteEvents and RemoteEvents:FindFirstChild("QuestAbandon")
local AllocateStat = RemoteEvents and RemoteEvents:FindFirstChild("AllocateStat")
local ObservationHakiRemote = RemoteEvents and RemoteEvents:FindFirstChild("ObservationHakiRemote")
local GetPlayerStats = RemoteEvents and RemoteEvents:FindFirstChild("GetPlayerStats")
local CombatSystem = ReplicatedStorage:FindFirstChild("CombatSystem")
local AbilitySystem = ReplicatedStorage:FindFirstChild("AbilitySystem")
local RequestAbility = AbilitySystem and AbilitySystem:FindFirstChild("Remotes") and AbilitySystem.Remotes:FindFirstChild("RequestAbility")
local RequestHit = CombatSystem and CombatSystem:FindFirstChild("Remotes") and CombatSystem.Remotes:FindFirstChild("RequestHit")
local RemotesFolder = ReplicatedStorage:FindFirstChild("Remotes")
local ShowNotification = RemotesFolder and RemotesFolder:FindFirstChild("ShowNotification")
local TeleportToPortal = RemotesFolder and RemotesFolder:FindFirstChild("TeleportToPortal")
local UseItem = RemotesFolder and RemotesFolder:FindFirstChild("UseItem")
local UpdateInventory = RemotesFolder and RemotesFolder:FindFirstChild("UpdateInventory")
local RequestSummonBoss = RemotesFolder and RemotesFolder:FindFirstChild("RequestSummonBoss")
local BossTimerSync = RemotesFolder and RemotesFolder:FindFirstChild("BossTimerSync")
local BossTimerEvent = RemotesFolder and RemotesFolder:FindFirstChild("BossTimerEvent")
local BossUIShow = RemotesFolder and RemotesFolder:FindFirstChild("BossUIShow")
local OpenInfiniteTowerUI = RemotesFolder and RemotesFolder:FindFirstChild("OpenInfiniteTowerUI")
local RequestDungeonPortal = RemotesFolder and RemotesFolder:FindFirstChild("RequestDungeonPortal")
local DungeonWaveVote = RemotesFolder and RemotesFolder:FindFirstChild("DungeonWaveVote")
local NPCs = Workspace:WaitForChild("NPCs", 5)

-- // GLOBAL STATE //
local function ServerHop()
    task.spawn(function()
        local sfUrl = "https://games.roblox.com/v1/games/%d/servers/Public?sortOrder=Asc&limit=100"
        local success, req = pcall(function() return request({Url = string.format(sfUrl, game.PlaceId)}) end)
        if success and req and req.Body then
            local body = HttpService:JSONDecode(req.Body)
            if body and body.data then
                local servers = body.data
                for i = #servers, 2, -1 do
                    local j = math.random(1, i)
                    servers[i], servers[j] = servers[j], servers[i]
                end
                for _, s in ipairs(servers) do
                    if type(s) == "table" and s.playing and s.maxPlayers and s.playing < s.maxPlayers and s.id ~= game.JobId then
                        pcall(function() game:GetService("TeleportService"):TeleportToPlaceInstance(game.PlaceId, s.id, LocalPlayer) end)
                        task.wait(1)
                        break -- Stop after initiating the first valid teleport to prevent crashing
                    end
                end
            end
        end
    end)
end

local AfkHopTimerLabel = nil
local PityCounterLabel = nil
local PityCounterLabel2 = nil
_G.JomHub_BossPity = _G.JomHub_BossPity or 0

local S = {
    AutoFarmActive = false,
    AutoLevelActive = false,
    AutoQuestActive = false,
    AutoSetSpawnActive = false,
    AutoStatsActive = false,
    EspEnabled = false,
    EspPlayers = false,
    EspEnemies = false,
    EspFruits = false,
    EspBossTimers = false,
    selectedMobs = {},
    autoFarmItems = false,
    itemFarmTakeQuests = true,
    itemFarmDuration = 60,
    farmCommon = {},
    farmRare = {},
    farmEpic = {},
    farmLegendary = {},
    farmMythical = {},
    farmSecret = {},
    farmPos = "Above",
    farmDistance = 10,
    teleportMode = "Tween",
    fastAttack = 1,
    autoMelee = false,
    autoSword = false,
    autoFruit = false,
    autoObservation = false,
    autoSkills = {},
    mainFarmMode = "Single Target",
    questName = "",
    lastAutoQuest = "",
    forceQuestAccept = false,
    autoKillAuraQuest = false,
    lastPortal = "",
    lastTargetName = "",
    lastBossHuntTarget = "",
    claimedSpawns = {},
    lastTargetedCrystalPos = nil,
    statAmount = 1,
    autoOpenChests = false,
    chestsToOpen = {},
    autoDungeonDiscovery = false,
    dungeonQuestAccepted = false,
    checkedDungeonPieces = {},
    currentDungeonIsland = "",
    autoHogyoku = false,
    hogyokuQuestAccepted = false,
    checkedHogyokuPieces = {},
    autoSlimeDiscovery = false,
    slimeQuestAccepted = false,
    checkedSlimePieces = {},
    autoDemonite = false,
    demoniteQuestAccepted = false,
    checkedDemonitePieces = {},
    auto25Bosses = false,
    autoDungeon = false,
    autoDungeonRestart = false,
    dungeonType = "CidDungeon",
    dungeonDiff = "Easy",
    dungeonPos = "Above",
    dungeonFarmMode = "Kill Aura",
    autoHuntBoss = false,
    autoHuntBossNoHop = false,
    huntBosses = {},
    huntHopTimer = 60,
    pityBuilderBosses = {},
    pitySummonBoss = "SaberBoss",
    pitySummonDiff = "Normal",
    autoSummonPity = false,
    autoHopPity = false,
    autoRaceReroll = false,
    targetRaces = {},
    autoClanReroll = false,
    afkAura = false,
    localAuraDist = 50,
    afkIslandHop = false,
    islandHopMode = "Kill Aura",
    afkIslandHopDist = 50,
    afkHopWhenCleared = false,
    afkIslands = {},
    afkHopDelay = 60,
    targetClans = {},
    autoBuyMerchant = false,
    buyMerchantItems = {},
    stats = {
        Melee = false,
        Defense = false,
        Sword = false,
        DevilFruit = false
    },
    misc = { ws = 16, jp = 50, noclip = false, infjump = false, wsEnabled = false, jpEnabled = false, antiRagdoll = false }
}

local function mergeConfig(dst, src)
    for k, v in pairs(src) do
        if type(v) == "table" and type(dst[k]) == "table" then
            mergeConfig(dst[k], v)
        else
            dst[k] = v
        end
    end
end

if readfile and isfile and isfolder then
    if isfile("JomHUB_Configs/SailorPiece_autoload.txt") then
        local cfgName = readfile("JomHUB_Configs/SailorPiece_autoload.txt")
        if isfile("JomHUB_Configs/"..cfgName..".json") then
            local success, data = pcall(function() return HttpService:JSONDecode(readfile("JomHUB_Configs/"..cfgName..".json")) end)
            if success and type(data) == "table" then
                mergeConfig(S, data)
            end
        end
    end
end

local InventoryCache = {}
local HasReceivedInventory = false
if UpdateInventory then
    UpdateInventory.OnClientEvent:Connect(function(action, data)
        if action == "Items" and type(data) == "table" then
            HasReceivedInventory = true
            local temp = {}
            for _, item in ipairs(data) do
                if type(item) == "table" and item.name then
                    temp[item.name] = item.quantity or 1
                end
            end
            InventoryCache = temp
        end
    end)
end

local BossTimersCache = {}
if BossTimerSync then
    BossTimerSync.OnClientEvent:Connect(function(data)
        if type(data) == "table" then
            for k, v in pairs(data) do
                BossTimersCache[k] = v
            end
        end
    end)
end

if BossTimerEvent then
    BossTimerEvent.OnClientEvent:Connect(function(bossName, state, timer)
        if type(bossName) == "string" then
            if not BossTimersCache[bossName] then BossTimersCache[bossName] = {} end
            BossTimersCache[bossName].state = state
            BossTimersCache[bossName].timer = tonumber(timer) or 0
            BossTimersCache[bossName].isAlive = (state == "SPAWNED")
        end
    end)
end

if BossUIShow then
    BossUIShow.OnClientEvent:Connect(function(data)
        if type(data) == "table" and data.pity ~= nil then
            _G.JomHub_BossPity = tonumber(data.pity) or 0
            if PityCounterLabel then
                PityCounterLabel.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25"
            end
            if PityCounterLabel2 then
                PityCounterLabel2.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25"
            end
        end
    end)
end

if OpenInfiniteTowerUI then
    OpenInfiniteTowerUI.OnClientEvent:Connect(function()
        if S.autoDungeon then
            if DungeonWaveVote then pcall(function() DungeonWaveVote:FireServer("start") end) end
            local pg = LocalPlayer:FindFirstChild("PlayerGui")
            if pg then
                local ui = pg:FindFirstChild("InfiniteTowerUI") or pg:FindFirstChild("TowerUI")
                if ui then ui.Enabled = false end
            end
        end
    end)
end

local HakiActive = false
local HakiCooldown = 0
if ObservationHakiRemote then
    ObservationHakiRemote.OnClientEvent:Connect(function(action, data)
        if action == "Activated" then
            HakiActive = true
        elseif action == "Deactivated" then
            HakiActive = false
            if type(data) == "table" and data.cooldown then
                HakiCooldown = tick() + data.cooldown
            end
        end
    end)
end

-- // LEVEL PROGRESSION MAP //
local PROGRESSION_MAP = {
    { Level = 1, Quest = "QuestNPC1", Mob = "Thief", Portal = "Starter" },
    { Level = 100, Quest = "QuestNPC2", Mob = "ThiefBoss", Portal = "Starter" },
    { Level = 250, Quest = "QuestNPC3", Mob = "Monkey", Portal = "Jungle" },
    { Level = 500, Quest = "QuestNPC4", Mob = "MonkeyBoss", Portal = "Jungle" },
    { Level = 750, Quest = "QuestNPC5", Mob = "DesertBandit", Portal = "Desert" },
    { Level = 1000, Quest = "QuestNPC6", Mob = "DesertBoss", Portal = "Desert" },
    { Level = 1500, Quest = "QuestNPC7", Mob = "FrostRogue", Portal = "Snow" },
    { Level = 2000, Quest = "QuestNPC8", Mob = "SnowBoss", Portal = "Snow" },
    { Level = 3000, Quest = "QuestNPC9", Mob = "Sorcerer", Portal = "Shibuya" },
    { Level = 4000, Quest = "QuestNPC10", Mob = "PandaMiniBoss", Portal = "Shibuya" },
    { Level = 5000, Quest = "QuestNPC11", Mob = "Hollow", Portal = "HollowIsland" },
    { Level = 6250, Quest = "QuestNPC12", Mob = "StrongSorcerer", Portal = "Shinjuku" },
    { Level = 7000, Quest = "QuestNPC13", Mob = "Curse", Portal = "Shinjuku" },
    { Level = 8000, Quest = "QuestNPC14", Mob = "Slime", Portal = "Slime" },
    { Level = 9000, Quest = "QuestNPC15", Mob = "AcademyTeacher", Portal = "Academy" },
    { Level = 10000, Quest = "QuestNPC16", Mob = "Swordsman", Portal = "Judgement" },
    { Level = 10750, Quest = "QuestNPC17", Mob = "Quincy", Portal = "SoulDominion" },
    { Level = 11500, Quest = "QuestNPC18", Mob = "Ninja", Portal = "Ninja" },
    { Level = 12000, Quest = "QuestNPC19", Mob = "ArenaFighter", Portal = "Lawless" }
}

local BOSS_LIST = {"AizenBoss", "AlucardBoss", "AnosBoss", "AtomicBoss", "BlessedMaidenBoss", "DesertBoss", "EscanorBoss", "GilgameshBoss", "GojoBoss", "IchigoBoss", "JinwooBoss", "MadokaBoss", "MonkeyBoss", "PandaMiniBoss", "QinShiBoss", "RagnaBoss", "RimuruBoss", "SaberAlterBoss", "SaberBoss", "ShadowBoss", "ShadowMonarchBoss", "SnowBoss", "StrongestinHistoryBoss", "StrongestofTodayBoss", "StrongestShinobiBoss", "SukunaBoss", "ThiefBoss", "TrueAizenBoss", "YamatoBoss", "YujiBoss"}


local ITEM_TARGETS = {
    ["Wood"] = "AutoLevel", ["Common Chest"] = "AutoLevel",
    ["Energy Core"] = "YujiBoss", ["Haki Color Reroll"] = "AllBosses", ["Iron"] = "AutoLevel", ["Rare Chest"] = "AutoLevel",
    ["Abyss Edge"] = "JinwooBoss", ["Awakened Cursed Finger"] = "StrongestinHistoryBoss", ["Black Frost"] = "RagnaBoss",
    ["Boss Ticket"] = "AllBosses", ["Broken Sword"] = "AutoLevel", ["Chrysalis Sigil"] = "AllBosses", ["Cursed Finger"] = "SukunaBoss", 
    ["Dark Grail"] = "AllBosses", ["Divine Grail"] = "AllBosses", ["Divine Fragment"] = "MadokaBoss", ["Dungeon Key"] = "AllBosses", ["Epic Chest"] = "AutoLevel", 
    ["Flash Impact"] = "YujiBoss", ["Fusion Ring"] = "TrueAizenBoss", ["Heart"] = "MadokaBoss", ["Illusion Prism"] = "AizenBoss",
    ["Limitless Key"] = "GojoBoss", ["Limitless Ring"] = "GojoBoss", ["Malevolent Key"] = "SukunaBoss", 
    ["Mirage Pen"] = "AizenBoss", ["Monarch Core"] = "ShadowMonarchBoss", ["Morgan Remnant"] = "SaberAlterBoss", ["Obsidian"] = "AutoLevel", 
    ["Race Reroll"] = "AllBosses", ["Reversal Pulse"] = "StrongestofTodayBoss", ["Sage Pulse"] = "RimuruBoss", 
    ["Slime Shard"] = "Slime", ["Soul Fragment"] = "IchigoBoss", ["Tempest Relic"] = "AllBosses", ["Throne Remnant"] = "GilgameshBoss", ["Tide Remnant"] = "BlessedMaidenBoss",
    ["Trait Reroll"] = "AllBosses", ["Umbral Capsule"] = "ShadowBoss", ["Vessel Ring"] = "StrongestinHistoryBoss",
    ["Void Fragment"] = "GojoBoss", ["Worthiness Fragment"] = "Hollow", ["Wyrm Brand"] = "RagnaBoss",
    ["Alter Essence"] = "SaberAlterBoss", ["Ancient Shard"] = "GilgameshBoss", ["Blue Singularity"] = "StrongestofTodayBoss", ["Calamity Seal"] = "AutoLevel", 
    ["Clan Reroll"] = "AutoLevel", ["Cursed Talisman"] = "Curse", ["Dark Ring"] = "JinwooBoss", 
    ["Dismantle Fang"] = "SukunaBoss", ["Divergent Pulse"] = "YujiBoss", ["Divinity Essence"] = "TrueAizenBoss", ["Energy Shard"] = "StrongSorcerer", ["Gale Essence"] = "BlessedMaidenBoss", 
    ["Golden Essence"] = "GilgameshBoss", ["Infinity Core"] = "GojoBoss", ["Jade Tablet"] = "QinShiBoss", 
    ["Legendary Chest"] = "AutoLevel", ["Malevolent Soul"] = "StrongestinHistoryBoss", ["Monarch Essence"] = "ShadowMonarchBoss", 
    ["Mythril"] = "AutoLevel", ["Radiant Core"] = "MadokaBoss", ["Reiatsu Core"] = "AizenBoss", 
    ["Sacred Bow"] = "MadokaBoss", ["Shadow Essence"] = "ShadowMonarchBoss", ["Silver Requiem"] = "RagnaBoss", 
    ["Six Eye"] = "StrongestofTodayBoss", ["Slime Remnant"] = "RimuruBoss", ["Soul Amulet"] = "AlucardBoss", 
    ["Spiritual Core"] = "IchigoBoss", ["Tempest Seal"] = "RimuruBoss", ["Void Seed"] = "ShadowBoss",
    ["Adamantite"] = "Hollow", ["Aero Core"] = "BlessedMaidenBoss", ["Alter Armor"] = "SaberAlterBoss", ["Atomic Core"] = "ShadowBoss", ["Blood Ring"] = "AlucardBoss", 
    ["Casull"] = "AlucardBoss", ["Celestial Mark"] = "BlessedMaidenBoss", ["Conqueror Fragment"] = "AutoLevel", ["Corrupt Crown"] = "SaberAlterBoss", ["Corruption Core"] = "SaberAlterBoss", ["Crimson Heart"] = "SukunaBoss", 
    ["Cursed Flesh"] = "StrongestinHistoryBoss", ["Evolution Fragment"] = "TrueAizenBoss", ["Gilgamesh Armor"] = "GilgameshBoss", ["Hogyoku Fragment"] = "AizenBoss", ["Imperial Seal"] = "QinShiBoss", 
    ["Infinity Essence"] = "StrongestofTodayBoss", ["Kamish Dagger"] = "ShadowMonarchBoss", ["Maiden Outfit"] = "BlessedMaidenBoss", ["Manipulator Outfit"] = "TrueAizenBoss", ["Mythical Chest"] = "AutoLevel", 
    ["Phantasm Core"] = "GilgameshBoss", ["Pink Gem"] = "MadokaBoss", ["Shadow Crystal"] = "ShadowMonarchBoss", 
    ["Shadow Heart"] = "JinwooBoss", ["Slime Core"] = "RimuruBoss", ["Soul Flame"] = "IchigoBoss", ["Transcendent Core"] = "TrueAizenBoss",
    ["Aura Crate"] = "SweepSlimeToSoul", ["Secret Chest"] = "SweepSlimeToSoul"}

local MOB_LIST = {
    "TrainingDummy", "Thief", "ThiefBoss", "Monkey", "MonkeyBoss",
    "DesertBandit", "DesertBoss", 
    "FrostRogue", "SnowBoss", "WinterWarden", "Winter Warden",
    "Sorcerer", "PandaMiniBoss", "Hollow", 
    "StrongSorcerer", "Strong Sorcerer", "Curse", 
    "Slime", "AcademyTeacher", 
    "Swordsman", "Quincy", "Ninja", "ArenaFighter",
    "AizenBoss", "AlucardBoss", "AnosBoss", "AtomicBoss", "BlessedMaidenBoss", "EscanorBoss", "GilgameshBoss", "GojoBoss", "IchigoBoss", "JinwooBoss", "MadokaBoss", "QinShiBoss", "RagnaBoss", "RimuruBoss", "SaberAlterBoss", "SaberBoss", "ShadowBoss", "ShadowMonarchBoss", "StrongestinHistoryBoss", "StrongestofTodayBoss", "StrongestShinobiBoss", "SukunaBoss", "TrueAizenBoss", "YamatoBoss", "YujiBoss"
}

local C = {
    BG         = Color3.fromRGB(5,  10, 18),
    TopBar     = Color3.fromRGB(8,  16, 28),
    Sidebar    = Color3.fromRGB(7,  13, 24),
    SectionBG  = Color3.fromRGB(11, 21, 36),
    SectionHd  = Color3.fromRGB(8,  17, 30),
    Card       = Color3.fromRGB(8,  16, 28),
    Divider    = Color3.fromRGB(22, 45, 75),
    Blue       = Color3.fromRGB(30, 140, 220),
    Cyan       = Color3.fromRGB(0,  200, 255),
    White      = Color3.fromRGB(210,235, 255),
    Gray       = Color3.fromRGB(100,145, 185),
    Muted      = Color3.fromRGB(38,  65,  95),
    ToggleOn   = Color3.fromRGB(30, 140, 220),
    ToggleOff  = Color3.fromRGB(20,  35,  55),
    BtnBG      = Color3.fromRGB(13,  28,  52),
    Green      = Color3.fromRGB(50, 215, 110),
    Orange     = Color3.fromRGB(255, 170, 0),
}

local function tw(o, p, t) TweenService:Create(o, TweenInfo.new(t or 0.15, Enum.EasingStyle.Quad), p):Play() end
local function rnd(p, r) local c = Instance.new("UICorner"); c.CornerRadius = UDim.new(0, r or 5); c.Parent = p end
local function mkLbl(parent, txt, size, color, bold, xAlign, z)
    local l = Instance.new("TextLabel")
    l.BackgroundTransparency = 1; l.Text = txt; l.TextSize = size or 14; l.TextColor3 = color or C.White
    l.Font = bold and Enum.Font.GothamBold or Enum.Font.Gotham; l.TextXAlignment = xAlign or Enum.TextXAlignment.Left
    l.TextYAlignment = Enum.TextYAlignment.Center; l.ZIndex = z or 2; l.Parent = parent
    return l
end

-- ============================================================
-- SCREEN GUI & WINDOW
-- ============================================================
local function getUiParent()
    local success, parent = pcall(function() return gethui() end)
    if not success or not parent then
        success, parent = pcall(function() return game:GetService("CoreGui") end)
    end
    if not success or not parent then
        parent = LocalPlayer:WaitForChild("PlayerGui")
    end
    return parent
end

local uiParent = getUiParent()
local existingGui = uiParent:FindFirstChild("JOMHUB")
if existingGui then existingGui:Destroy() end

local Gui = Instance.new("ScreenGui")
Gui.Name = "JOMHUB"; Gui.ResetOnSpawn = false; Gui.ZIndexBehavior = Enum.ZIndexBehavior.Global; Gui.DisplayOrder = 10; Gui.Parent = uiParent

-- LOADING SCREEN
local LoadFrame = Instance.new("Frame"); LoadFrame.Size = UDim2.new(0, 300, 0, 140); LoadFrame.Position = UDim2.new(0.5, 0, 0.5, 0); LoadFrame.AnchorPoint = Vector2.new(0.5, 0.5); LoadFrame.BackgroundColor3 = C.BG; LoadFrame.BorderSizePixel = 0; LoadFrame.ZIndex = 200; LoadFrame.Parent = Gui; rnd(LoadFrame, 8)
local LoadStroke = Instance.new("UIStroke"); LoadStroke.Thickness = 2; LoadStroke.Color = C.Cyan; LoadStroke.Parent = LoadFrame
local LoadTitle = mkLbl(LoadFrame, "JOMHUB", 28, C.Cyan, true, Enum.TextXAlignment.Center, 201); LoadTitle.Size = UDim2.new(1, 0, 0, 40); LoadTitle.Position = UDim2.new(0, 0, 0, 20)
local LoadSub = mkLbl(LoadFrame, "Initializing...", 12, C.White, false, Enum.TextXAlignment.Center, 201); LoadSub.Size = UDim2.new(1, 0, 0, 20); LoadSub.Position = UDim2.new(0, 0, 0, 65)
local LoadBarBg = Instance.new("Frame"); LoadBarBg.Size = UDim2.new(0.8, 0, 0, 6); LoadBarBg.Position = UDim2.new(0.1, 0, 0, 100); LoadBarBg.BackgroundColor3 = C.SectionBG; LoadBarBg.BorderSizePixel = 0; LoadBarBg.ZIndex = 201; LoadBarBg.Parent = LoadFrame; rnd(LoadBarBg, 3)
local LoadBarFill = Instance.new("Frame"); LoadBarFill.Size = UDim2.new(0, 0, 1, 0); LoadBarFill.BackgroundColor3 = C.Cyan; LoadBarFill.BorderSizePixel = 0; LoadBarFill.ZIndex = 202; LoadBarFill.Parent = LoadBarBg; rnd(LoadBarFill, 3)

task.spawn(function()
    local hue = 0
    while LoadFrame.Parent do
        hue = hue + 0.02
        if hue > 1 then hue = 0 end
        if LoadStroke.Parent then LoadStroke.Color = Color3.fromHSV(hue, 1, 1) end
        if LoadTitle.Parent then LoadTitle.TextColor3 = Color3.fromHSV(hue, 1, 1) end
        task.wait(0.05)
    end
end)

-- TOGGLE BUTTON
local TGlow = Instance.new("Frame"); TGlow.Size = UDim2.new(0, 82, 0, 36); TGlow.Position = UDim2.new(0, 9, 0, 9); TGlow.BackgroundColor3 = C.Cyan; TGlow.BorderSizePixel = 0; TGlow.ZIndex = 98; TGlow.Visible = false; TGlow.Parent = Gui; rnd(TGlow, 9)
local TGlowInner = Instance.new("Frame"); TGlowInner.Size = UDim2.new(0, 78, 0, 32); TGlowInner.Position = UDim2.new(0, 11, 0, 11); TGlowInner.BackgroundColor3 = C.BG; TGlowInner.BorderSizePixel = 0; TGlowInner.ZIndex = 99; TGlowInner.Visible = false; TGlowInner.Parent = Gui; rnd(TGlowInner, 8)
local TBtn = Instance.new("TextButton"); TBtn.Size = UDim2.new(0, 78, 0, 32); TBtn.Position = UDim2.new(0, 11, 0, 11); TBtn.BackgroundColor3 = C.BtnBG; TBtn.TextColor3 = C.Cyan; TBtn.Font = Enum.Font.GothamBold; TBtn.Text = "JomHUB"; TBtn.TextSize = 14; TBtn.BorderSizePixel = 0; TBtn.ZIndex = 100; TBtn.Visible = false; TBtn.Parent = Gui; rnd(TBtn, 8)

local tDrag, tDs, tGp, tGip, tBp
TBtn.InputBegan:Connect(function(i) if i.UserInputType == Enum.UserInputType.MouseButton1 or i.UserInputType == Enum.UserInputType.Touch then tDrag = true; tDs = i.Position; tGp = TGlow.Position; tGip = TGlowInner.Position; tBp = TBtn.Position end end)
TBtn.InputEnded:Connect(function(i) if i.UserInputType == Enum.UserInputType.MouseButton1 or i.UserInputType == Enum.UserInputType.Touch then tDrag = false end end)
UserInputService.InputChanged:Connect(function(i) if tDrag and (i.UserInputType == Enum.UserInputType.MouseMovement or i.UserInputType == Enum.UserInputType.Touch) then local d = i.Position - tDs; TGlow.Position = UDim2.new(tGp.X.Scale,  tGp.X.Offset  + d.X, tGp.Y.Scale,  tGp.Y.Offset  + d.Y); TGlowInner.Position = UDim2.new(tGip.X.Scale, tGip.X.Offset + d.X, tGip.Y.Scale, tGip.Y.Offset + d.Y); TBtn.Position = UDim2.new(tBp.X.Scale,  tBp.X.Offset  + d.X, tBp.Y.Scale,  tBp.Y.Offset  + d.Y) end end)

-- MAIN WINDOW
local Win = Instance.new("Frame"); Win.Size = UDim2.new(0, 650, 0, 450); Win.Position = UDim2.new(0.5, 0, 0.5, 0); Win.AnchorPoint = Vector2.new(0.5, 0.5); Win.BackgroundColor3 = C.BG; Win.BorderSizePixel = 0; Win.ZIndex = 2; Win.Visible = false; Win.Parent = Gui; rnd(Win, 8)
local winScale = Instance.new("UIScale"); winScale.Scale = 1; winScale.Parent = Win

local szConstraint = Instance.new("UISizeConstraint")
szConstraint.MinSize = Vector2.new(450, 350)
szConstraint.MaxSize = Vector2.new(800, 600)
szConstraint.Parent = Win

local function updateSize()
    local viewport = Workspace.CurrentCamera.ViewportSize
    local isMobile = viewport.X < 850 or UserInputService.TouchEnabled
    if isMobile then
        Win.Size = UDim2.new(0.75, 0, 0.80, 0)
        szConstraint.MinSize = Vector2.new(200, 200)
        szConstraint.MaxSize = Vector2.new(1000, 1000)
    else
        Win.Size = UDim2.new(0, 650, 0, 450)
        szConstraint.MinSize = Vector2.new(450, 350)
        szConstraint.MaxSize = Vector2.new(800, 600)
    end
end
updateSize()
Workspace.CurrentCamera:GetPropertyChangedSignal("ViewportSize"):Connect(updateSize)
UserInputService:GetPropertyChangedSignal("TouchEnabled"):Connect(updateSize)

local Top = Instance.new("Frame"); Top.Size = UDim2.new(1, 0, 0, 44); Top.BackgroundColor3 = C.TopBar; Top.BorderSizePixel = 0; Top.ZIndex = 50; Top.Parent = Win; rnd(Top, 8)
local topFill = Instance.new("Frame"); topFill.Size = UDim2.new(1, 0, 0, 10); topFill.Position = UDim2.new(0, 0, 1, -10); topFill.BackgroundColor3 = C.TopBar; topFill.BorderSizePixel = 0; topFill.ZIndex = 50; topFill.Parent = Top
local tDot = Instance.new("Frame"); tDot.Size = UDim2.new(0, 10, 0, 10); tDot.Position = UDim2.new(0, 14, 0.5, -5); tDot.BackgroundColor3 = C.Cyan; tDot.BorderSizePixel = 0; tDot.ZIndex = 51; tDot.Parent = Top; rnd(tDot, 5)
local tName = mkLbl(Top, "JOMHUB - SAILOR PIECE PREMIUM", 17, C.Cyan, true, Enum.TextXAlignment.Left, 51); tName.Size = UDim2.new(0.5, 0, 1, 0); tName.Position = UDim2.new(0, 32, 0, 0)
local tDiv = Instance.new("Frame"); tDiv.Size = UDim2.new(1, 0, 0, 1); tDiv.Position = UDim2.new(0, 0, 1, 0); tDiv.BackgroundColor3 = C.Divider; tDiv.BorderSizePixel = 0; tDiv.ZIndex = 51; tDiv.Parent = Top

local winStroke = Instance.new("UIStroke"); winStroke.Thickness = 2.5; winStroke.ApplyStrokeMode = Enum.ApplyStrokeMode.Border; winStroke.Color = C.Cyan; winStroke.Parent = Win

task.spawn(function()
    local hue = 0
    while _G.JomHubRunning do
        hue = hue + 0.005 
        if hue > 1 then hue = 0 end
        local rgbColor = Color3.fromHSV(hue, 1, 1)
        if winStroke.Parent then winStroke.Color = rgbColor end
        if tName.Parent then tName.TextColor3 = rgbColor end
        task.wait(0.03) 
    end
end)

local dragging, dragInput, dragStart, startPos
Top.InputBegan:Connect(function(input) if input.UserInputType == Enum.UserInputType.MouseButton1 or input.UserInputType == Enum.UserInputType.Touch then dragging = true; dragStart = input.Position; startPos = Win.Position; input.Changed:Connect(function() if input.UserInputState == Enum.UserInputState.End then dragging = false end end) end end)
Top.InputChanged:Connect(function(input) if input.UserInputType == Enum.UserInputType.MouseMovement or input.UserInputType == Enum.UserInputType.Touch then dragInput = input end end)
UserInputService.InputChanged:Connect(function(input) if input == dragInput and dragging then local delta = input.Position - dragStart; Win.Position = UDim2.new(startPos.X.Scale, startPos.X.Offset + delta.X, startPos.Y.Scale, startPos.Y.Offset + delta.Y) end end)

local vis = true
local activeDropdown = nil
TBtn.Activated:Connect(function() vis = not vis; Win.Visible = vis; tw(TBtn, {BackgroundColor3 = vis and C.BtnBG or C.Card}); tw(TBtn, {TextColor3 = vis and C.Cyan or C.Gray}); tw(TGlow, {BackgroundColor3 = vis and C.Cyan or C.Muted}); if not vis and activeDropdown then activeDropdown.Visible = false; activeDropdown = nil end end)

-- ============================================================
-- TABS & SCROLLING FRAMES
-- ============================================================
local SB = Instance.new("ScrollingFrame"); SB.Size = UDim2.new(0.13, 0, 1, -74); SB.Position = UDim2.new(0, 0, 0, 44); SB.BackgroundColor3 = C.Sidebar; SB.BorderSizePixel = 0; SB.ZIndex = 50; SB.ScrollBarThickness = 0; SB.AutomaticCanvasSize = Enum.AutomaticSize.Y; SB.CanvasSize = UDim2.new(0,0,0,0); SB.Parent = Win
local sbDiv = Instance.new("Frame"); sbDiv.Size = UDim2.new(0, 1, 1, -74); sbDiv.Position = UDim2.new(0.13, 0, 0, 44); sbDiv.BackgroundColor3 = C.Divider; sbDiv.BorderSizePixel = 0; sbDiv.ZIndex = 51; sbDiv.Parent = Win
local sbLayout = Instance.new("UIListLayout"); sbLayout.FillDirection = Enum.FillDirection.Vertical; sbLayout.Padding = UDim.new(0, 6); sbLayout.HorizontalAlignment = Enum.HorizontalAlignment.Center; sbLayout.SortOrder = Enum.SortOrder.LayoutOrder; sbLayout.Parent = SB
local sbPad = Instance.new("UIPadding"); sbPad.PaddingTop = UDim.new(0, 8); sbPad.Parent = SB

local function mkTabBtn(name, isFirst)
    local btn = Instance.new("TextButton"); btn.Size = UDim2.new(1, -10, 0, 34); btn.BackgroundColor3 = isFirst and C.Card or C.Sidebar; btn.Text = name; btn.TextColor3 = isFirst and C.Cyan or C.Muted; btn.Font = Enum.Font.GothamBold; btn.TextSize = 11; btn.BorderSizePixel = 0; btn.ZIndex = 51; btn.Parent = SB; rnd(btn, 4)
    local bar = Instance.new("Frame"); bar.Size = UDim2.new(0, 3, 0.6, 0); bar.Position = UDim2.new(0, 0, 0.2, 0); bar.BackgroundColor3 = C.Cyan; bar.BorderSizePixel = 0; bar.ZIndex = 52; bar.Visible = isFirst; bar.Parent = btn; rnd(bar, 2)
    return btn, bar
end

local TabMain, tmBar = mkTabBtn("MAIN", true)
local TabEsp, teBar = mkTabBtn("ESP", false)
local TabQuest, tqBar = mkTabBtn("QUEST", false)
local TabFarm, tfaBar = mkTabBtn("ITEM FARM", false)
local TabDungeon, tdBar = mkTabBtn("DUNGEON", false)
local TabAfk, taBar = mkTabBtn("AFK", false)
local TabShop, tsBar = mkTabBtn("SHOP", false)
local TabMisc, tmiBar = mkTabBtn("MISC", false)
local TabSetting, tseBar = mkTabBtn("SETTING", false)

local function mkScroll()
    local sc = Instance.new("ScrollingFrame"); sc.Size = UDim2.new(0.87, 0, 1, -74); sc.Position = UDim2.new(0.13, 0, 0, 44); sc.BackgroundTransparency = 1; sc.BorderSizePixel = 0; sc.ScrollBarThickness = 3; sc.ScrollBarImageColor3 = C.Blue; sc.AutomaticCanvasSize = Enum.AutomaticSize.Y; sc.CanvasSize = UDim2.new(0,0,0,0); sc.ElasticBehavior = Enum.ElasticBehavior.Never; sc.Visible = false; sc.Parent = Win
    local list = Instance.new("UIListLayout"); list.FillDirection = Enum.FillDirection.Vertical; list.Padding = UDim.new(0, 10); list.Parent = sc
    local pad = Instance.new("UIPadding"); pad.PaddingTop = UDim.new(0, 10); pad.PaddingBottom = UDim.new(0, 10); pad.PaddingLeft = UDim.new(0, 10); pad.PaddingRight = UDim.new(0, 10); pad.Parent = sc
    return sc
end

local MainScroll = mkScroll(); MainScroll.Visible = true
local EspScroll = mkScroll()
local QuestScroll = mkScroll()
local FarmScroll = mkScroll()
local DungeonScroll = mkScroll()
local AfkScroll = mkScroll()
local ShopScroll = mkScroll()
local MiscScroll = mkScroll()
local SettingScroll = mkScroll()

local tabs = {
    {btn=TabMain, bar=tmBar, scroll=MainScroll},
    {btn=TabEsp, bar=teBar, scroll=EspScroll},
    {btn=TabQuest, bar=tqBar, scroll=QuestScroll},
    {btn=TabFarm, bar=tfaBar, scroll=FarmScroll},
    {btn=TabDungeon, bar=tdBar, scroll=DungeonScroll},
    {btn=TabAfk, bar=taBar, scroll=AfkScroll},
    {btn=TabShop, bar=tsBar, scroll=ShopScroll},
    {btn=TabMisc, bar=tmiBar, scroll=MiscScroll},
    {btn=TabSetting, bar=tseBar, scroll=SettingScroll},
}

for _, t in ipairs(tabs) do
    t.btn.Activated:Connect(function()
        if activeDropdown then activeDropdown.Visible = false; activeDropdown = nil end
        for _, ot in ipairs(tabs) do
            ot.scroll.Visible = (t == ot)
            tw(ot.btn, {BackgroundColor3 = (t == ot) and C.Card or C.Sidebar, TextColor3 = (t == ot) and C.Cyan or C.Muted})
            ot.bar.Visible = (t == ot)
        end
    end)
end

-- ============================================================
-- UI BUILDERS
-- ============================================================
_G.UI_Updaters = {Toggles={}, Sliders={}, DDs={}, TextBoxes={}, MultiDDs={}}

local function mkSection(parent, title, bodyH)
    local sec = Instance.new("Frame"); sec.Size = UDim2.new(1, 0, 0, 36+bodyH); sec.BackgroundColor3 = C.SectionBG; sec.BorderSizePixel = 0; sec.Parent = parent; rnd(sec, 6)
    local hd = Instance.new("Frame"); hd.Size = UDim2.new(1, 0, 0, 36); hd.BackgroundColor3 = C.SectionHd; hd.BorderSizePixel = 0; hd.ZIndex = 2; hd.Parent = sec; rnd(hd, 6)
    local hdf = Instance.new("Frame"); hdf.Size = UDim2.new(1, 0, 0, 10); hdf.Position = UDim2.new(0, 0, 1, -10); hdf.BackgroundColor3 = C.SectionHd; hdf.BorderSizePixel = 0; hdf.ZIndex = 2; hdf.Parent = hd
    local acBar = Instance.new("Frame"); acBar.Size = UDim2.new(0, 4, 0, 18); acBar.Position = UDim2.new(0, 10, 0.5, -9); acBar.BackgroundColor3 = C.Blue; acBar.BorderSizePixel = 0; acBar.ZIndex = 3; acBar.Parent = hd; rnd(acBar, 2)
    mkLbl(hd, title, 13, C.Cyan, true, Enum.TextXAlignment.Center, 3).Size = UDim2.new(1, 0, 1, 0)
    local body = Instance.new("Frame"); body.Name = "Body"; body.Size = UDim2.new(1, 0, 1, -38); body.Position = UDim2.new(0, 0, 0, 38); body.BackgroundTransparency = 1; body.Parent = sec
    return sec, body
end

local function mkToggle(parent, label, default, yPos, onChange, xPos)
    local actX = xPos or 0.02
    local row = Instance.new("Frame"); row.Size = UDim2.new(0.46, 0, 0, 34); row.Position = UDim2.new(actX, 4, 0, yPos); row.BackgroundColor3 = C.Card; row.BorderSizePixel = 0; row.ZIndex = 2; row.Parent = parent; rnd(row, 5)
    local rl = mkLbl(row, label, 12, C.Gray, true, Enum.TextXAlignment.Left, 3); rl.Size = UDim2.new(1, -70, 1, 0); rl.Position = UDim2.new(0, 8, 0, 0)
    local track = Instance.new("TextButton"); track.Size = UDim2.new(0, 40, 0, 20); track.Position = UDim2.new(1, -46, 0.5, -10); track.BackgroundColor3 = default and C.ToggleOn or C.ToggleOff; track.Text = ""; track.BorderSizePixel = 0; track.ZIndex = 3; track.Parent = row; rnd(track, 10)
    local thumb = Instance.new("Frame"); thumb.AnchorPoint = Vector2.new(0, 0.5); thumb.Size = UDim2.new(0, 14, 0, 14); thumb.Position = default and UDim2.new(0, 24, 0.5, 0) or UDim2.new(0, 2, 0.5, 0); thumb.BackgroundColor3 = Color3.fromRGB(255,255,255); thumb.BorderSizePixel = 0; thumb.ZIndex = 4; thumb.Parent = track; rnd(thumb, 7)
    local on = default
    local function setVisual(state)
        on = state
        tw(track, {BackgroundColor3 = on and C.ToggleOn or C.ToggleOff})
        tw(thumb, {Position = on and UDim2.new(0, 24, 0.5, 0) or UDim2.new(0, 2, 0.5, 0)})
    end
    track.Activated:Connect(function() setVisual(not on); if onChange then onChange(on) end end)
    _G.UI_Updaters.Toggles[label] = setVisual
end

local function mkSlider(parent, label, min, max, default, yPos, callback, xPos)
    local actX = xPos or 0.52
    local safeDefault = default or min
    local bg = Instance.new("Frame"); bg.Size = UDim2.new(0.46, 0, 0, 34); bg.Position = UDim2.new(actX, 4, 0, yPos); bg.BackgroundColor3 = C.Card; bg.BorderSizePixel = 0; bg.ZIndex = 2; bg.Parent = parent; rnd(bg, 5)
    local lbl = mkLbl(bg, label..": "..tostring(safeDefault), 9, C.Gray, true, Enum.TextXAlignment.Center, 3); lbl.Size = UDim2.new(1, 0, 0, 14); lbl.Position = UDim2.new(0, 0, 0, 2)
    local bar = Instance.new("Frame"); bar.Size = UDim2.new(0.8, 0, 0, 4); bar.Position = UDim2.new(0.1, 0, 0, 22); bar.BackgroundColor3 = C.ToggleOff; bar.BorderSizePixel = 0; bar.ZIndex = 3; bar.Parent = bg; rnd(bar, 2)
    local fill = Instance.new("Frame"); fill.Size = UDim2.new((safeDefault-min)/(max-min), 0, 1, 0); fill.BackgroundColor3 = C.Blue; fill.BorderSizePixel = 0; fill.ZIndex = 4; fill.Parent = bar; rnd(fill, 2)
    local btn = Instance.new("TextButton"); btn.Size = UDim2.new(1, 0, 1, 0); btn.BackgroundTransparency = 1; btn.Text = ""; btn.ZIndex = 5; btn.Parent = bar
    
    local function setVisual(val)
        local x = math.clamp((val - min) / (max - min), 0, 1)
        fill.Size = UDim2.new(x, 0, 1, 0)
        lbl.Text = label..": "..val
    end

    local dragging = false
    btn.InputBegan:Connect(function(i) if i.UserInputType == Enum.UserInputType.MouseButton1 or i.UserInputType == Enum.UserInputType.Touch then dragging = true end end)
    UserInputService.InputEnded:Connect(function(i) if i.UserInputType == Enum.UserInputType.MouseButton1 or i.UserInputType == Enum.UserInputType.Touch then dragging = false end end)
    UserInputService.InputChanged:Connect(function(i) if dragging and (i.UserInputType == Enum.UserInputType.MouseMovement or i.UserInputType == Enum.UserInputType.Touch) then
        local x = math.clamp((i.Position.X - bar.AbsolutePosition.X) / bar.AbsoluteSize.X, 0, 1)
        local val = math.floor(min + (max-min)*x + 0.5)
        fill.Size = UDim2.new(x, 0, 1, 0); lbl.Text = label..": "..val; if callback then callback(val) end
    end end)
    _G.UI_Updaters.Sliders[label] = setVisual
end

local function mkStart(parent, label, yPos, color, onStart, onStop, xPos)
    local actX = xPos or 0.52
    local btn = Instance.new("TextButton")
    btn.Size = UDim2.new(0.46, 0, 0, 34); btn.Position = UDim2.new(actX, 4, 0, yPos); btn.BackgroundColor3 = C.BtnBG; btn.Text = "START  "..label; btn.TextColor3 = color or C.Cyan; btn.Font = Enum.Font.GothamBold; btn.TextSize = 11; btn.BorderSizePixel = 0; btn.ZIndex = 3; btn.Parent = parent; rnd(btn, 5)
    local run = false
    btn.Activated:Connect(function() run = not run; if run then btn.Text = "STOP  "..label; tw(btn, {BackgroundColor3 = Color3.fromRGB(20,48,95)}); if onStart then onStart() end else btn.Text = "START  "..label; tw(btn, {BackgroundColor3 = C.BtnBG}); if onStop then onStop() end end end)
    btn.MouseEnter:Connect(function() if not run then tw(btn,{BackgroundColor3=Color3.fromRGB(15,34,65)}) end end); btn.MouseLeave:Connect(function() if not run then tw(btn,{BackgroundColor3=C.BtnBG}) end end)
    return btn
end

local function mkBtn(parent, label, yPos, callback, xPos)
    local actX = xPos or 0.52
    local btn = Instance.new("TextButton")
    btn.Size = UDim2.new(0.46, 0, 0, 34)
    btn.Position = UDim2.new(actX, 4, 0, yPos)
    btn.BackgroundColor3 = C.BtnBG
    btn.Text = label
    btn.TextColor3 = C.Cyan
    btn.Font = Enum.Font.GothamBold
    btn.TextSize = 11
    btn.BorderSizePixel = 0
    btn.ZIndex = 3
    btn.Parent = parent
    rnd(btn, 5)
    btn.Activated:Connect(function() if callback then callback() end end)
    return btn
end

local function mkTextBox(parent, placeholder, yPos, callback, xPos)
    local actX = xPos or 0.52
    local bg = Instance.new("Frame"); bg.Size = UDim2.new(0.46, 0, 0, 34); bg.Position = UDim2.new(actX, 4, 0, yPos); bg.BackgroundColor3 = C.Card; bg.BorderSizePixel = 0; bg.ZIndex = 2; bg.ClipsDescendants = true; bg.Parent = parent; rnd(bg, 5)
    local box = Instance.new("TextBox"); box.Size = UDim2.new(1, -10, 1, 0); box.Position = UDim2.new(0, 5, 0, 0); box.BackgroundTransparency = 1; box.Text = ""; box.PlaceholderText = placeholder; box.TextColor3 = C.White; box.PlaceholderColor3 = C.Gray; box.Font = Enum.Font.Gotham; box.TextSize = 12; box.ZIndex = 3; box.Parent = bg
    box.FocusLost:Connect(function() if callback then callback(box.Text) end end)
    box:GetPropertyChangedSignal("Text"):Connect(function() if callback then callback(box.Text) end end)
    local function setVisual(val) box.Text = tostring(val) end
    _G.UI_Updaters.TextBoxes[placeholder] = setVisual
    return box
end

local function mkDD(parent, title, items, yTop, onPick, showSelectAll, xPos)
    local actX = xPos or 0.02
    local IH = 32; local MAX_VIS = 5; local DDH = math.min(#items, MAX_VIS) * IH
    
    local selLbl = mkLbl(parent, title .. ": NONE", 10, C.Gray, false, Enum.TextXAlignment.Left, 3)
    selLbl.Size = UDim2.new(0.46, 0, 0, 18); selLbl.Position = UDim2.new(actX, 4, 0, yTop); selLbl.TextTruncate = Enum.TextTruncate.AtEnd 

    local btn = Instance.new("TextButton")
    btn.Size = UDim2.new(0.46, 0, 0, 34); btn.Position = UDim2.new(actX, 4, 0, yTop + 20); btn.BackgroundColor3 = C.Card; btn.BorderSizePixel = 0; btn.Text = ""; btn.ZIndex = 8; btn.Parent = parent; rnd(btn, 5)

    local vl = mkLbl(btn, "Select...", 11, C.Cyan, false, Enum.TextXAlignment.Left, 9)
    vl.Size = UDim2.new(1, -30, 1, 0); vl.Position = UDim2.new(0, 8, 0, 0); vl.TextTruncate = Enum.TextTruncate.AtEnd 

    local al = mkLbl(btn, "v", 11, C.Blue, true, Enum.TextXAlignment.Center, 9)
    al.Size = UDim2.new(0, 22, 1, 0); al.Position = UDim2.new(1, -24, 0, 0)

    local lf = Instance.new("Frame")
    lf.BackgroundColor3 = C.SectionHd; lf.BorderSizePixel = 0; lf.ZIndex = 150; lf.Visible = false; lf.ClipsDescendants = true; lf.Parent = Win; rnd(lf, 5)
    local lfStroke = Instance.new("UIStroke"); lfStroke.Color = C.Blue; lfStroke.Thickness = 1.5; lfStroke.Parent = lf

    local sf = Instance.new("ScrollingFrame")
    sf.Size = UDim2.new(1, 0, 1, 0); sf.CanvasSize = UDim2.new(0, 0, 0, #items * IH); sf.AutomaticCanvasSize = Enum.AutomaticSize.None; sf.ScrollBarThickness = 3; sf.ScrollBarImageColor3 = C.Blue; sf.BackgroundTransparency = 1; sf.BorderSizePixel = 0; sf.ZIndex = 151; sf.ElasticBehavior = Enum.ElasticBehavior.Never; sf.Active = true; sf.Parent = lf
    local ll = Instance.new("UIListLayout"); ll.FillDirection = Enum.FillDirection.Vertical; ll.Parent = sf

    for _, item in ipairs(items) do
        local ib = Instance.new("TextButton")
        ib.Size = UDim2.new(1, 0, 0, IH); ib.BackgroundColor3 = C.SectionHd; ib.BackgroundTransparency = 1; ib.Text = "  "..item; ib.TextColor3 = C.Gray; ib.Font = Enum.Font.Gotham; ib.TextSize = 11; ib.BorderSizePixel = 0; ib.ZIndex = 152; ib.Parent = sf; ib.TextXAlignment = Enum.TextXAlignment.Left
        ib.MouseEnter:Connect(function() ib.BackgroundTransparency = 0; ib.BackgroundColor3 = Color3.fromRGB(14, 32, 60) end)
        ib.MouseLeave:Connect(function() ib.BackgroundTransparency = 1 end)
        ib.Activated:Connect(function()
            vl.Text = item; selLbl.Text = title .. ": " .. string.upper(item)
            lf.Visible = false; activeDropdown = nil
            if onPick then onPick(item) end
        end)
    end

    btn.Activated:Connect(function()
        local willOpen = not lf.Visible
        if activeDropdown and activeDropdown ~= lf then activeDropdown.Visible = false end
        if willOpen then
            local relX = btn.AbsolutePosition.X - Win.AbsolutePosition.X
            local relY = btn.AbsolutePosition.Y - Win.AbsolutePosition.Y + btn.AbsoluteSize.Y
            if btn.AbsolutePosition.Y + btn.AbsoluteSize.Y + DDH > Gui.AbsoluteSize.Y then relY = btn.AbsolutePosition.Y - Win.AbsolutePosition.Y - DDH end
            lf.Position = UDim2.new(0, relX, 0, relY); lf.Size = UDim2.new(0, btn.AbsoluteSize.X, 0, DDH)
        end
        lf.Visible = willOpen
        activeDropdown = willOpen and lf or nil
    end)
    
    for _, t in ipairs(tabs) do
        t.scroll:GetPropertyChangedSignal("CanvasPosition"):Connect(function() if lf.Visible then lf.Visible = false; activeDropdown = nil end end)
    end
    
    local function setVisual(val)
        vl.Text = val or "Select..."
        selLbl.Text = title .. ": " .. string.upper(val or "NONE")
    end
    _G.UI_Updaters.DDs[title] = setVisual
    return btn, vl, selLbl
end

local function mkMultiDD(parent, title, items, yTop, onPick, showSelectAll, xPos, defaultDict)
    local actX = xPos or 0.02
    local extra = showSelectAll and 1 or 0
    local IH = 32; local MAX_VIS = 5; local DDH = math.min(#items + extra, MAX_VIS) * IH
    local selectedDict = defaultDict or {} 
    local itemButtons = {}

    local selLbl = mkLbl(parent, title .. ": NONE", 10, C.Gray, false, Enum.TextXAlignment.Left, 3)
    selLbl.Size = UDim2.new(0.46, 0, 0, 18); selLbl.Position = UDim2.new(actX, 4, 0, yTop); selLbl.TextTruncate = Enum.TextTruncate.AtEnd 

    local btn = Instance.new("TextButton")
    btn.Size = UDim2.new(0.46, 0, 0, 34); btn.Position = UDim2.new(actX, 4, 0, yTop + 20); btn.BackgroundColor3 = C.Card; btn.BorderSizePixel = 0; btn.Text = ""; btn.ZIndex = 8; btn.Parent = parent; rnd(btn, 5)

    local vl = mkLbl(btn, "Select...", 11, C.Cyan, false, Enum.TextXAlignment.Left, 9)
    vl.Size = UDim2.new(1, -30, 1, 0); vl.Position = UDim2.new(0, 8, 0, 0); vl.TextTruncate = Enum.TextTruncate.AtEnd 

    local al = mkLbl(btn, "v", 11, C.Blue, true, Enum.TextXAlignment.Center, 9)
    al.Size = UDim2.new(0, 22, 1, 0); al.Position = UDim2.new(1, -24, 0, 0)

    local lf = Instance.new("Frame")
    lf.BackgroundColor3 = C.SectionHd; lf.BorderSizePixel = 0; lf.ZIndex = 150; lf.Visible = false; lf.ClipsDescendants = true; lf.Parent = Win; rnd(lf, 5)
    local lfStroke = Instance.new("UIStroke"); lfStroke.Color = C.Blue; lfStroke.Thickness = 1.5; lfStroke.Parent = lf

    local sf = Instance.new("ScrollingFrame")
    sf.Size = UDim2.new(1, 0, 1, 0); sf.CanvasSize = UDim2.new(0, 0, 0, (#items + extra) * IH); sf.AutomaticCanvasSize = Enum.AutomaticSize.None; sf.ScrollBarThickness = 3; sf.ScrollBarImageColor3 = C.Blue; sf.BackgroundTransparency = 1; sf.BorderSizePixel = 0; sf.ZIndex = 151; sf.ElasticBehavior = Enum.ElasticBehavior.Never; sf.Active = true; sf.Parent = lf
    local ll = Instance.new("UIListLayout"); ll.FillDirection = Enum.FillDirection.Vertical; ll.Parent = sf

    local function updateLabel()
        local count = 0; local lastName = ""
        for k, _ in pairs(selectedDict) do count = count + 1; lastName = k end
        if count == 0 then vl.Text = "Select..."; selLbl.Text = title .. ": NONE"
        elseif count == #items then vl.Text = "All Selected"; selLbl.Text = title .. ": ALL"
        elseif count == 1 then vl.Text = lastName; selLbl.Text = title .. ": " .. lastName:upper()
        else vl.Text = tostring(count) .. " Items Selected"; selLbl.Text = title .. ": " .. tostring(count) .. " ITEMS" end
    end

    if showSelectAll then
        local allBtn = Instance.new("TextButton")
        allBtn.Size = UDim2.new(1, 0, 0, IH); allBtn.BackgroundColor3 = C.SectionHd; allBtn.BackgroundTransparency = 1; allBtn.Text = "  [ SELECT ALL ]"; allBtn.TextColor3 = C.Cyan; allBtn.Font = Enum.Font.GothamBold; allBtn.TextSize = 11; allBtn.BorderSizePixel = 0; allBtn.ZIndex = 152; allBtn.Parent = sf; allBtn.TextXAlignment = Enum.TextXAlignment.Left
        allBtn.MouseEnter:Connect(function() allBtn.BackgroundTransparency = 0; allBtn.BackgroundColor3 = Color3.fromRGB(14, 32, 60) end)
        allBtn.MouseLeave:Connect(function() allBtn.BackgroundTransparency = 1 end)

        allBtn.Activated:Connect(function()
            local anyUnselected = false
            for _, item in ipairs(items) do if not selectedDict[item] then anyUnselected = true; break end end
            for _, item in ipairs(items) do
                selectedDict[item] = anyUnselected and true or nil
                local btn = itemButtons[item]
                if btn then btn.TextColor3 = anyUnselected and C.Cyan or C.Gray; btn.Font = anyUnselected and Enum.Font.GothamBold or Enum.Font.Gotham end
            end
            updateLabel()
            if onPick then onPick(selectedDict) end
        end)
    end

    for _, item in ipairs(items) do
        local ib = Instance.new("TextButton")
        ib.Size = UDim2.new(1, 0, 0, IH); ib.BackgroundColor3 = C.SectionHd; ib.BackgroundTransparency = 1; ib.Text = "  "..item; ib.TextColor3 = C.Gray; ib.Font = Enum.Font.Gotham; ib.TextSize = 11; ib.BorderSizePixel = 0; ib.ZIndex = 152; ib.Parent = sf; ib.TextXAlignment = Enum.TextXAlignment.Left

        itemButtons[item] = ib
        local isSelected = false
        ib.MouseEnter:Connect(function() ib.BackgroundTransparency = 0; ib.BackgroundColor3 = Color3.fromRGB(14, 32, 60) end)
        ib.MouseLeave:Connect(function() ib.BackgroundTransparency = 1 end)
        
        ib.Activated:Connect(function()
            isSelected = not isSelected
            if isSelected then
                selectedDict[item] = true; ib.TextColor3 = C.Cyan; ib.Font = Enum.Font.GothamBold
            else
                selectedDict[item] = nil; ib.TextColor3 = C.Gray; ib.Font = Enum.Font.Gotham
            end
            ib.Text = "  " .. item 
            updateLabel()
            if onPick then onPick(selectedDict) end
        end)
    end

    updateLabel()
    btn.Activated:Connect(function()
        local willOpen = not lf.Visible
        if activeDropdown and activeDropdown ~= lf then activeDropdown.Visible = false end
        if willOpen then
            local relX = btn.AbsolutePosition.X - Win.AbsolutePosition.X
            local relY = btn.AbsolutePosition.Y - Win.AbsolutePosition.Y + btn.AbsoluteSize.Y
            if btn.AbsolutePosition.Y + btn.AbsoluteSize.Y + DDH > Gui.AbsoluteSize.Y then relY = btn.AbsolutePosition.Y - Win.AbsolutePosition.Y - DDH end
            lf.Position = UDim2.new(0, relX, 0, relY); lf.Size = UDim2.new(0, btn.AbsoluteSize.X, 0, DDH)
        end
        lf.Visible = willOpen
        activeDropdown = willOpen and lf or nil
    end)
    
    for _, t in ipairs(tabs) do
        t.scroll:GetPropertyChangedSignal("CanvasPosition"):Connect(function() if lf.Visible then lf.Visible = false; activeDropdown = nil end end)
    end
    
    local function setVisual(dict)
        selectedDict = dict or {}
        for item, ib in pairs(itemButtons) do
            if selectedDict[item] then ib.TextColor3 = C.Cyan; ib.Font = Enum.Font.GothamBold
            else ib.TextColor3 = C.Gray; ib.Font = Enum.Font.Gotham end
        end
        updateLabel()
    end
    _G.UI_Updaters.MultiDDs[title] = setVisual

    return btn, vl, selLbl
end

-- ============================================================
-- STATUS BAR
-- ============================================================
local Stat = Instance.new("Frame"); Stat.Size = UDim2.new(1, 0, 0, 30); Stat.Position = UDim2.new(0, 0, 1, -30); Stat.BackgroundColor3 = C.TopBar; Stat.BorderSizePixel = 0; Stat.ZIndex = 50; Stat.Parent = Win
local stTop = Instance.new("Frame"); stTop.Size = UDim2.new(1, 0, 0, 1); stTop.BackgroundColor3 = C.Divider; stTop.BorderSizePixel = 0; stTop.ZIndex = 51; stTop.Parent = Stat
local sDot = Instance.new("Frame"); sDot.Size = UDim2.new(0, 8, 0, 8); sDot.Position = UDim2.new(0, 12, 0.5, -4); sDot.BackgroundColor3 = C.Cyan; sDot.BorderSizePixel = 0; sDot.ZIndex = 51; sDot.Parent = Stat; rnd(sDot, 4)
local sTxt = mkLbl(Stat,"SYSTEM ONLINE  --  AWAITING COMMAND",9,C.Muted,false,Enum.TextXAlignment.Left,51); sTxt.Size = UDim2.new(0.6, 0, 1, 0); sTxt.Position = UDim2.new(0, 26, 0, 0)
local sVer = mkLbl(Stat,"JOMHUB v1",9,C.Muted,false,Enum.TextXAlignment.Right,51); sVer.Size = UDim2.new(0.3, 0, 1, 0); sVer.Position = UDim2.new(0.7, -10, 0, 0)

local function UpdateStatus(text, color)
    if sTxt then
        sTxt.Text = text
        sTxt.TextColor3 = color or C.Muted
    end
end

local function RefreshUI()
    if not _G.UI_Updaters then return end
    local u = _G.UI_Updaters
    local function safeCall(cat, key, val)
        if u[cat] and u[cat][key] then u[cat][key](val) end
    end
    
    safeCall("Toggles", "AUTO EQUIP MELEE", S.autoMelee)
    safeCall("Toggles", "AUTO EQUIP SWORD", S.autoSword)
    safeCall("Toggles", "AUTO EQUIP FRUIT", S.autoFruit)
    safeCall("Toggles", "AUTO HUNT & HOP", S.autoHuntBoss)
    safeCall("Toggles", "AUTO HUNT (NO HOP)", S.autoHuntBossNoHop)
    safeCall("Toggles", "AUTO ACCEPT & COMPLETE", S.AutoQuestActive)
    safeCall("Toggles", "AUTO SUMMON PITY", S.autoSummonPity)
    safeCall("Toggles", "START AUTO HOP PITY", S.autoHopPity)
    safeCall("Toggles", "AUTO LEVEL", S.AutoLevelActive)
    safeCall("Toggles", "AUTO SET SPAWN", S.AutoSetSpawnActive)
    safeCall("Toggles", "AUTO QUEST AURA", S.autoKillAuraQuest)
    safeCall("Toggles", "AUTO HOGYOKU DISCOVERY", S.autoHogyoku)
    safeCall("Toggles", "AUTO DISCOVERY (PIECES)", S.autoDungeonDiscovery)
    safeCall("Toggles", "AUTO SLIME DISCOVERY", S.autoSlimeDiscovery)
    safeCall("Toggles", "AUTO DEMONITE CORES", S.autoDemonite)
    safeCall("Toggles", "AUTO 25 BOSSES", S.auto25Bosses)
    safeCall("Toggles", "AUTO FARM ITEMS", S.autoFarmItems)
    safeCall("Toggles", "TAKE QUESTS", S.itemFarmTakeQuests)
    safeCall("Toggles", "ENABLE ESP", S.EspEnabled)
    safeCall("Toggles", "ESP PLAYERS", S.EspPlayers)
    safeCall("Toggles", "ESP ENEMIES", S.EspEnemies)
    safeCall("Toggles", "ESP FRUITS", S.EspFruits)
    safeCall("Toggles", "ESP BOSS TIMERS", S.EspBossTimers)
    safeCall("Toggles", "AUTO DUNGEON", S.autoDungeon)
    safeCall("Toggles", "AUTO RESTART (SAVE)", S.autoDungeonRestart)
    safeCall("Toggles", "ENABLE AURA KILL", S.afkAura)
    safeCall("Toggles", "ENABLE ISLAND HOP", S.afkIslandHop)
    safeCall("Toggles", "HOP WHEN CLEARED", S.afkHopWhenCleared)
    safeCall("Toggles", "AUTO MELEE", S.stats.Melee)
    safeCall("Toggles", "AUTO SWORD", S.stats.Sword)
    safeCall("Toggles", "AUTO DEFENSE", S.stats.Defense)
    safeCall("Toggles", "AUTO DEVIL FRUIT", S.stats.DevilFruit)
    safeCall("Toggles", "WALKSPEED", S.misc.wsEnabled)
    safeCall("Toggles", "JUMPPOWER", S.misc.jpEnabled)
    safeCall("Toggles", "NOCLIP", S.misc.noclip)
    safeCall("Toggles", "INFINITE JUMP", S.misc.infjump)
    safeCall("Toggles", "ANTI RAGDOLL", S.misc.antiRagdoll)
    safeCall("Toggles", "AUTO OBS HAKI", S.autoObservation)
    safeCall("Toggles", "AUTO OPEN CHESTS", S.autoOpenChests)
    safeCall("Toggles", "AUTO RACE REROLL", S.autoRaceReroll)
    safeCall("Toggles", "AUTO CLAN REROLL", S.autoClanReroll)
    safeCall("Toggles", "AUTO BUY MERCHANT", S.autoBuyMerchant)

    safeCall("Sliders", "DISTANCE", S.farmDistance)
    safeCall("Sliders", "FAST ATTACK", S.fastAttack)
    safeCall("Sliders", "TIME PER ITEM (SEC)", S.itemFarmDuration)
    safeCall("Sliders", "LOCAL AURA DIST", S.localAuraDist)
    safeCall("Sliders", "HOP DELAY (SEC)", S.afkHopDelay)
    safeCall("Sliders", "HOP AURA DIST", S.afkIslandHopDist)
    safeCall("Sliders", "SPEED", S.misc.ws)
    safeCall("Sliders", "POWER", S.misc.jp)
    safeCall("Sliders", "HOP IF TIMER > (SEC)", S.huntHopTimer)

    safeCall("DDs", "FARM POSITION", S.farmPos)
    safeCall("DDs", "TELEPORT MODE", S.teleportMode)
    safeCall("DDs", "ATTACK MODE", S.mainFarmMode)
    safeCall("DDs", "SELECT QUEST", S.questName)
    safeCall("DDs", "DUNGEON TYPE", S.dungeonType)
    safeCall("DDs", "DIFFICULTY", S.dungeonDiff)
    safeCall("DDs", "DUNGEON POS", S.dungeonPos)
    safeCall("DDs", "DUNGEON ATTACK MODE", S.dungeonFarmMode)
    safeCall("DDs", "2. TARGET BOSS", S.pitySummonBoss)
    safeCall("DDs", "3. DIFFICULTY", S.pitySummonDiff)
    safeCall("DDs", "TARGET BOSS (HOP)", S.pitySummonBoss)
    safeCall("DDs", "DIFFICULTY (HOP)", S.pitySummonDiff)

    safeCall("MultiDDs", "TARGET MOBS", S.selectedMobs)
    safeCall("MultiDDs", "AUTO SKILLS", S.autoSkills)
    safeCall("MultiDDs", "TARGET BOSSES", S.huntBosses)
    safeCall("MultiDDs", "1. BUILDER BOSSES", S.pityBuilderBosses)
    safeCall("MultiDDs", "COMMON", S.farmCommon)
    safeCall("MultiDDs", "RARE", S.farmRare)
    safeCall("MultiDDs", "EPIC", S.farmEpic)
    safeCall("MultiDDs", "LEGENDARY", S.farmLegendary)
    safeCall("MultiDDs", "MYTHICAL", S.farmMythical)
    safeCall("MultiDDs", "SECRET", S.farmSecret)
    safeCall("MultiDDs", "SELECT ISLANDS", S.afkIslands)
    safeCall("MultiDDs", "SELECT CHESTS", S.chestsToOpen)
    safeCall("MultiDDs", "TARGET RACES", S.targetRaces)
    safeCall("MultiDDs", "TARGET CLANS", S.targetClans)
    safeCall("MultiDDs", "MERCHANT ITEMS", S.buyMerchantItems)

    safeCall("TextBoxes", "Add Amount (e.g. 1)", tostring(S.statAmount))
end

-- ============================================================
-- UI CONTENTS
-- ============================================================

-- Dynamically scan workspace to pre-populate quest givers
local QUEST_LIST = {}
pcall(function()
    for _, v in ipairs(Workspace:GetDescendants()) do
        if v:IsA("ProximityPrompt") and string.find(v.Name, "Quest") then
            local model = v:FindFirstAncestorOfClass("Model")
            if model and not table.find(QUEST_LIST, model.Name) then
                table.insert(QUEST_LIST, model.Name)
            end
        end
    end
end)
local fallbackQs = {
    "QuestNPC1", "QuestNPC2", "QuestNPC3", "QuestNPC4", "QuestNPC5",
    "QuestNPC6", "QuestNPC7", "QuestNPC8", "QuestNPC9", "QuestNPC10",
    "QuestNPC11", "QuestNPC12", "QuestNPC13", "QuestNPC14", "QuestNPC15",
    "QuestNPC16", "QuestNPC17"
}
for _, fq in ipairs(fallbackQs) do
    if not table.find(QUEST_LIST, fq) then table.insert(QUEST_LIST, fq) end
end

pcall(function()
    if NPCs then
        for _, npc in ipairs(NPCs:GetChildren()) do
            local cleanName = npc.Name:gsub("%d+$", ""):match("^%s*(.-)%s*$")
            if cleanName and not table.find(MOB_LIST, cleanName) then
                table.insert(MOB_LIST, cleanName)
            end
        end
    end
end)

do
    local M_BODY = 382
    local mSec, mBdy = mkSection(MainScroll, "AUTO FARM", M_BODY)
    
    -- Left Column
    mkMultiDD(mBdy, "TARGET MOBS", MOB_LIST, 8, function(dict) S.selectedMobs = dict end, true, 0.02, S.selectedMobs)
    mkMultiDD(mBdy, "AUTO SKILLS", {"Skill Z", "Skill X", "Skill C", "Skill V", "Skill F"}, 70, function(dict) S.autoSkills = dict end, true, 0.02, S.autoSkills)
    mkDD(mBdy, "FARM POSITION", {"Above", "Below", "Front", "Behind", "Left", "Right"}, 132, function(val) S.farmPos = val end, false, 0.02)
    mkDD(mBdy, "TELEPORT MODE", {"Tween", "Instant"}, 194, function(val) S.teleportMode = val end, false, 0.02)
    mkDD(mBdy, "ATTACK MODE", {"Kill Aura", "Single Target"}, 256, function(val) S.mainFarmMode = val end, false, 0.02)

    
    -- Right Column
    mkStart(mBdy, "MANUAL FARM", 28, C.Cyan, function() S.AutoFarmActive = true end, function() S.AutoFarmActive = false end, 0.52)
    mkToggle(mBdy, "AUTO EQUIP MELEE", S.autoMelee, 90, function(v) S.autoMelee = v end, 0.52)
    mkToggle(mBdy, "AUTO EQUIP SWORD", S.autoSword, 152, function(v) S.autoSword = v end, 0.52)
    mkToggle(mBdy, "AUTO EQUIP FRUIT", S.autoFruit, 214, function(v) S.autoFruit = v end, 0.52)
    mkSlider(mBdy, "DISTANCE", 0, 20, S.farmDistance, 276, function(v) S.farmDistance = v end, 0.52)
    mkSlider(mBdy, "FAST ATTACK", 1, 10, S.fastAttack, 318, function(v) S.fastAttack = v end, 0.52)
end

do
    local BH_BODY = 140
    local bhSec, bhBdy = mkSection(MainScroll, "AUTO HUNT BOSS", BH_BODY)
    
    mkMultiDD(bhBdy, "TARGET BOSSES", BOSS_LIST, 8, function(dict) S.huntBosses = dict end, true, 0.02, S.huntBosses)
    mkToggle(bhBdy, "AUTO HUNT & HOP", S.autoHuntBoss, 28, function(v)
        S.autoHuntBoss = v
        if writefile then
            if not isfolder("JomHUB_Configs") then pcall(makefolder, "JomHUB_Configs") end
            if v then
                local bossHuntConfig = {
                    autoHuntBoss = true,
                    huntBosses = S.huntBosses,
                    autoMelee = S.autoMelee,
                    autoSword = S.autoSword,
                    autoFruit = S.autoFruit,
                    autoSkills = S.autoSkills,
                    fastAttack = S.fastAttack,
                    teleportMode = S.teleportMode,
                    farmDistance = S.farmDistance,
                    farmPos = S.farmPos,
                    huntHopTimer = S.huntHopTimer
                }
                local s, data = pcall(function() return HttpService:JSONEncode(bossHuntConfig) end)
                if s then pcall(writefile, "JomHUB_Configs/SailorPiece_BossHunt.json", data) end
                pcall(writefile, "JomHUB_Configs/SailorPiece_autoload.txt", "SailorPiece_BossHunt")
                UpdateStatus("BOSS HUNT AUTO-START ENABLED!", C.Green)
            else
                pcall(writefile, "JomHUB_Configs/SailorPiece_autoload.txt", "")
                UpdateStatus("BOSS HUNT AUTO-START CLEARED!", C.Gray)
            end
        end
    end, 0.52)
    mkToggle(bhBdy, "AUTO HUNT (NO HOP)", S.autoHuntBossNoHop, 70, function(v) S.autoHuntBossNoHop = v end, 0.02)
    mkSlider(bhBdy, "HOP IF TIMER > (SEC)", 0, 600, S.huntHopTimer, 70, function(v) S.huntHopTimer = v end, 0.52)
    local hHint = mkLbl(bhBdy, "Hops random servers to hunt selected bosses.", 9, C.Gray, false, Enum.TextXAlignment.Center, 3)
    hHint.Size = UDim2.new(0.96, 0, 0, 20); hHint.Position = UDim2.new(0.02, 0, 0, 112)
end

local PITY_BOSSES = {}
for _, b in ipairs(BOSS_LIST) do
    if b ~= "DesertBoss" and b ~= "MonkeyBoss" and b ~= "PandaMiniBoss" and b ~= "ThiefBoss" and b ~= "SnowBoss" then
        table.insert(PITY_BOSSES, b)
    end
end

do
    local PITY_BODY = 190
    local pitySec, pityBdy = mkSection(MainScroll, "AUTO PITY SYSTEM", PITY_BODY)
    
    local SUMMON_BOSSES = {"AtomicBoss", "TrueAizenBoss", "AnosBoss", "RimuruBoss", "StrongestofTodayBoss", "StrongestinHistoryBoss", "SaberBoss", "QinShiBoss", "IchigoBoss", "GilgameshBoss", "BlessedMaidenBoss", "SaberAlterBoss"}
    mkMultiDD(pityBdy, "1. BUILDER BOSSES", SUMMON_BOSSES, 8, function(dict) S.pityBuilderBosses = dict end, true, 0.02, S.pityBuilderBosses)
    mkDD(pityBdy, "2. TARGET BOSS", PITY_BOSSES, 8, function(v) S.pitySummonBoss = v end, false, 0.52)
    mkDD(pityBdy, "3. DIFFICULTY", {"Normal", "Medium", "Hard", "Extreme"}, 70, function(v) S.pitySummonDiff = v end, false, 0.02)
    mkTextBox(pityBdy, "4. Type Current Pity Here..", 70, function(t) _G.JomHub_BossPity = tonumber(t) or 0; if PityCounterLabel then PityCounterLabel.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25" end end, 0.52)
    
    mkToggle(pityBdy, "START AUTO PITY", S.autoSummonPity, 132, function(v) S.autoSummonPity = v end, 0.02)
    PityCounterLabel = mkLbl(pityBdy, "Current Pity: " .. (_G.JomHub_BossPity or 0) .. "/25", 11, C.Cyan, true, Enum.TextXAlignment.Center, 3)
    PityCounterLabel.Size = UDim2.new(0.46, 0, 0, 34); PityCounterLabel.Position = UDim2.new(0.52, 4, 0, 132); PityCounterLabel.BackgroundColor3 = C.Card; PityCounterLabel.BackgroundTransparency = 0; rnd(PityCounterLabel, 5)
end

do
    local AHP_BODY = 140
    local ahpSec, ahpBdy = mkSection(MainScroll, "AUTO HOP PITY", AHP_BODY)
    
    local SUMMON_BOSSES = {"AtomicBoss", "TrueAizenBoss", "AnosBoss", "RimuruBoss", "StrongestofTodayBoss", "StrongestinHistoryBoss", "SaberBoss", "QinShiBoss", "IchigoBoss", "GilgameshBoss", "BlessedMaidenBoss", "SaberAlterBoss"}
    mkDD(ahpBdy, "TARGET BOSS (HOP)", PITY_BOSSES, 8, function(v) S.pitySummonBoss = v end, false, 0.02)
    mkDD(ahpBdy, "DIFFICULTY (HOP)", {"Normal", "Medium", "Hard", "Extreme"}, 8, function(v) S.pitySummonDiff = v end, false, 0.52)
    
    mkTextBox(ahpBdy, "Set Pity (0-24)", 70, function(t) _G.JomHub_BossPity = tonumber(t) or 0; if PityCounterLabel then PityCounterLabel.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25" end; if PityCounterLabel2 then PityCounterLabel2.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25" end end, 0.02)
    mkToggle(ahpBdy, "START AUTO HOP PITY", S.autoHopPity, 70, function(v)
        S.autoHopPity = v
        if writefile then
            if not isfolder("JomHUB_Configs") then pcall(makefolder, "JomHUB_Configs") end
            if v then
                local pityHopConfig = {
                    autoHopPity = true,
                    pitySummonBoss = S.pitySummonBoss,
                    pitySummonDiff = S.pitySummonDiff,
                    autoMelee = S.autoMelee,
                    autoSword = S.autoSword,
                    autoFruit = S.autoFruit,
                    autoSkills = S.autoSkills,
                    fastAttack = S.fastAttack,
                    teleportMode = S.teleportMode,
                    farmDistance = S.farmDistance,
                    farmPos = S.farmPos,
                    mainFarmMode = S.mainFarmMode
                }
                local s, data = pcall(function() return HttpService:JSONEncode(pityHopConfig) end)
                if s then pcall(writefile, "JomHUB_Configs/SailorPiece_PityHop.json", data) end
                pcall(writefile, "JomHUB_Configs/SailorPiece_autoload.txt", "SailorPiece_PityHop")
                UpdateStatus("PITY HOP AUTO-START ENABLED!", C.Green)
            else
                pcall(writefile, "JomHUB_Configs/SailorPiece_autoload.txt", "")
                UpdateStatus("PITY HOP AUTO-START CLEARED!", C.Gray)
            end
        end
    end, 0.52)
    
    PityCounterLabel2 = mkLbl(ahpBdy, "Current Pity: " .. (_G.JomHub_BossPity or 0) .. "/25", 11, C.Cyan, true, Enum.TextXAlignment.Center, 3)
    PityCounterLabel2.Size = UDim2.new(0.96, 0, 0, 34); PityCounterLabel2.Position = UDim2.new(0.02, 4, 0, 112); PityCounterLabel2.BackgroundColor3 = C.Card; PityCounterLabel2.BackgroundTransparency = 0; rnd(PityCounterLabel2, 5)
end

do
    local Q_BODY = 190
    local qSec, qBdy = mkSection(QuestScroll, "AUTO QUEST", Q_BODY)
    
    mkDD(qBdy, "SELECT QUEST", QUEST_LIST, 8, function(v) S.questName = v end, false, 0.02)
    mkToggle(qBdy, "AUTO ACCEPT & COMPLETE", S.AutoQuestActive, 28, function(v) S.AutoQuestActive = v end, 0.52)
    
    mkToggle(qBdy, "AUTO LEVEL", S.AutoLevelActive, 70, function(v) S.AutoLevelActive = v end, 0.02)
    mkToggle(qBdy, "AUTO SET SPAWN", S.AutoSetSpawnActive, 70, function(v) S.AutoSetSpawnActive = v end, 0.52)
    
    mkToggle(qBdy, "AUTO QUEST AURA", false, 112, function(v) S.autoKillAuraQuest = v end, 0.02)
    
    local qHint = mkLbl(qBdy, "Aura kills all task enemies instantly.", 10, C.Gray, false, Enum.TextXAlignment.Center, 3)
    qHint.Size = UDim2.new(1, 0, 0, 20); qHint.Position = UDim2.new(0, 0, 0, 154)
end

do
    local HQ_BODY = 200
    local hqSec, hqBdy = mkSection(QuestScroll, "QUESTS", HQ_BODY)
    
    mkToggle(hqBdy, "AUTO HOGYOKU DISCOVERY", false, 8, function(v) 
        S.autoHogyoku = v 
        if v then S.checkedHogyokuPieces = {}; S.hogyokuQuestAccepted = false end
    end, 0.02)
    local hqHint = mkLbl(hqBdy, "Teleports and collects 6 Hogyoku fragments.", 9, C.Gray, false, Enum.TextXAlignment.Center, 3)
    hqHint.Size = UDim2.new(0.46, 0, 0, 20); hqHint.Position = UDim2.new(0.02, 4, 0, 42)
    
    mkToggle(hqBdy, "AUTO DISCOVERY (PIECES)", false, 8, function(v) 
        S.autoDungeonDiscovery = v 
        if v then S.checkedDungeonPieces = {}; S.dungeonQuestAccepted = false end
    end, 0.52)
    local dHint = mkLbl(hqBdy, "Teleports and collects 6 puzzle pieces.", 9, C.Gray, false, Enum.TextXAlignment.Center, 3)
    dHint.Size = UDim2.new(0.46, 0, 0, 20); dHint.Position = UDim2.new(0.52, 4, 0, 42)
    
    mkToggle(hqBdy, "AUTO SLIME DISCOVERY", false, 70, function(v) 
        S.autoSlimeDiscovery = v 
        if v then S.checkedSlimePieces = {}; S.slimeQuestAccepted = false end
    end, 0.02)
    local sHint = mkLbl(hqBdy, "Collects 7 Slime pieces.", 9, C.Gray, false, Enum.TextXAlignment.Center, 3)
    sHint.Size = UDim2.new(0.46, 0, 0, 20); sHint.Position = UDim2.new(0.02, 4, 0, 104)
    
    mkToggle(hqBdy, "AUTO DEMONITE CORES", false, 70, function(v) 
        S.autoDemonite = v 
        if v then S.checkedDemonitePieces = {}; S.demoniteQuestAccepted = false end
    end, 0.52)
    local dmHint = mkLbl(hqBdy, "Collects 2 Demonite Cores.", 9, C.Gray, false, Enum.TextXAlignment.Center, 3)
    dmHint.Size = UDim2.new(0.46, 0, 0, 20); dmHint.Position = UDim2.new(0.52, 4, 0, 104)
    
    mkToggle(hqBdy, "AUTO 25 BOSSES", false, 132, function(v) S.auto25Bosses = v end, 0.02)
    local bHint = mkLbl(hqBdy, "Farms Yuji for keys, summons Saber.", 9, C.Gray, false, Enum.TextXAlignment.Center, 3)
    bHint.Size = UDim2.new(0.46, 0, 0, 20); bHint.Position = UDim2.new(0.02, 4, 0, 166)
end

do
    local ITEM_BODY = 382
    local iSec, iBdy = mkSection(FarmScroll, "AUTO FARM ITEMS", ITEM_BODY)
    
    -- Left Column (Rarity Selection)
    mkMultiDD(iBdy, "COMMON", {"Common Chest", "Wood"}, 8, function(dict) S.farmCommon = dict end, true, 0.02, S.farmCommon)
    mkMultiDD(iBdy, "RARE", {"Energy Core", "Haki Color Reroll", "Iron", "Rare Chest"}, 70, function(dict) S.farmRare = dict end, true, 0.02, S.farmRare)
    mkMultiDD(iBdy, "EPIC", {"Abyss Edge", "Awakened Cursed Finger", "Black Frost", "Boss Ticket", "Broken Sword", "Chrysalis Sigil", "Cursed Finger", "Dark Grail", "Divine Fragment", "Divine Grail", "Dungeon Key", "Epic Chest", "Flash Impact", "Fusion Ring", "Heart", "Illusion Prism", "Limitless Key", "Limitless Ring", "Malevolent Key", "Mirage Pen", "Monarch Core", "Morgan Remnant", "Obsidian", "Race Reroll", "Reversal Pulse", "Sage Pulse", "Slime Shard", "Soul Fragment", "Tempest Relic", "Throne Remnant", "Tide Remnant", "Trait Reroll", "Umbral Capsule", "Vessel Ring", "Void Fragment", "Worthiness Fragment", "Wyrm Brand"}, 132, function(dict) S.farmEpic = dict end, true, 0.02, S.farmEpic)
    mkMultiDD(iBdy, "LEGENDARY", {"Alter Essence", "Ancient Shard", "Blue Singularity", "Calamity Seal", "Clan Reroll", "Cursed Talisman", "Dark Ring", "Dismantle Fang", "Divergent Pulse", "Divinity Essence", "Energy Shard", "Gale Essence", "Golden Essence", "Infinity Core", "Jade Tablet", "Legendary Chest", "Malevolent Soul", "Monarch Essence", "Mythril", "Radiant Core", "Reiatsu Core", "Sacred Bow", "Shadow Essence", "Silver Requiem", "Six Eye", "Slime Remnant", "Soul Amulet", "Spiritual Core", "Tempest Seal", "Void Seed"}, 194, function(dict) S.farmLegendary = dict end, true, 0.02, S.farmLegendary)
    mkMultiDD(iBdy, "MYTHICAL", {"Adamantite", "Aero Core", "Alter Armor", "Atomic Core", "Blood Ring", "Casull", "Celestial Mark", "Conqueror Fragment", "Corrupt Crown", "Corruption Core", "Crimson Heart", "Cursed Flesh", "Evolution Fragment", "Gilgamesh Armor", "Hogyoku Fragment", "Imperial Seal", "Infinity Essence", "Kamish Dagger", "Maiden Outfit", "Manipulator Outfit", "Mythical Chest", "Phantasm Core", "Pink Gem", "Shadow Crystal", "Shadow Heart", "Slime Core", "Soul Flame", "Transcendent Core"}, 256, function(dict) S.farmMythical = dict end, true, 0.02, S.farmMythical)
    mkMultiDD(iBdy, "SECRET", {"Aura Crate", "Secret Chest"}, 318, function(dict) S.farmSecret = dict end, true, 0.02, S.farmSecret)
    
    -- Right Column (Execution)
    mkToggle(iBdy, "AUTO FARM ITEMS", S.autoFarmItems, 8, function(v) S.autoFarmItems = v end, 0.52)
    mkToggle(iBdy, "TAKE QUESTS", S.itemFarmTakeQuests, 50, function(v) S.itemFarmTakeQuests = v end, 0.52)
    mkSlider(iBdy, "TIME PER ITEM (SEC)", 10, 600, S.itemFarmDuration, 92, function(v) S.itemFarmDuration = v end, 0.52)
    
    local fHint = mkLbl(iBdy, "Loops through your selected items, hunting specific bosses or enemies.", 10, C.Gray, false, Enum.TextXAlignment.Center, 3)
    fHint.Size = UDim2.new(0.46, 0, 0, 40); fHint.Position = UDim2.new(0.52, 4, 0, 134); fHint.TextWrapped = true
end

do
    local E_BODY = 140
    local eSec, eBdy = mkSection(EspScroll, "ESP SETTINGS", E_BODY)
    
    mkToggle(eBdy, "ENABLE ESP", S.EspEnabled, 8, function(v) S.EspEnabled = v end, 0.02)
    mkToggle(eBdy, "ESP PLAYERS", S.EspPlayers, 50, function(v) S.EspPlayers = v end, 0.02)
    
    mkToggle(eBdy, "ESP ENEMIES", S.EspEnemies, 8, function(v) S.EspEnemies = v end, 0.52)
    mkToggle(eBdy, "ESP FRUITS", S.EspFruits, 50, function(v) S.EspFruits = v end, 0.52)
    
    mkToggle(eBdy, "ESP BOSS TIMERS", S.EspBossTimers, 92, function(v) S.EspBossTimers = v end, 0.02)
end

do
    local D_BODY = 194
    local dSec, dBdy = mkSection(DungeonScroll, "DUNGEON", D_BODY)
    
    mkDD(dBdy, "DUNGEON TYPE", {"CidDungeon", "RuneDungeon", "DoubleDungeon", "BossRush", "InfiniteTower"}, 8, function(v) S.dungeonType = v end, false, 0.02)
    mkToggle(dBdy, "AUTO DUNGEON", S.autoDungeon, 28, function(v) S.autoDungeon = v end, 0.52)
    
    mkDD(dBdy, "DIFFICULTY", {"Easy", "Medium", "Hard", "Extreme"}, 70, function(v) S.dungeonDiff = v end, false, 0.02)
    mkToggle(dBdy, "AUTO RESTART (SAVE)", S.autoDungeonRestart, 90, function(v)
        S.autoDungeonRestart = v
        S.autoDungeon = v
        if writefile then
            if not isfolder("JomHUB_Configs") then pcall(makefolder, "JomHUB_Configs") end
            if v then
                local dungeonConfig = {
                    autoDungeonRestart = true,
                    autoDungeon = true,
                    dungeonType = S.dungeonType,
                    dungeonDiff = S.dungeonDiff,
                    dungeonPos = S.dungeonPos,
                    dungeonFarmMode = S.dungeonFarmMode,
                    autoMelee = S.autoMelee,
                    autoSword = S.autoSword,
                    autoFruit = S.autoFruit,
                    autoSkills = S.autoSkills,
                    fastAttack = S.fastAttack,
                    teleportMode = S.teleportMode,
                    farmDistance = S.farmDistance
                }
                local s, data = pcall(function() return HttpService:JSONEncode(dungeonConfig) end)
                if s then pcall(writefile, "JomHUB_Configs/SailorPiece_Dungeon.json", data) end
                pcall(writefile, "JomHUB_Configs/SailorPiece_autoload.txt", "SailorPiece_Dungeon")
                UpdateStatus("DUNGEON AUTO-START ENABLED!", C.Green)
            else
                pcall(writefile, "JomHUB_Configs/SailorPiece_autoload.txt", "")
                UpdateStatus("DUNGEON AUTO-START CLEARED!", C.Gray)
            end
        end
    end, 0.52)
    
    mkDD(dBdy, "DUNGEON POS", {"Above", "Below", "Front", "Behind", "Left", "Right"}, 132, function(val) S.dungeonPos = val end, false, 0.02)
    mkDD(dBdy, "DUNGEON ATTACK MODE", {"Kill Aura", "Single Target"}, 132, function(val) S.dungeonFarmMode = val end, false, 0.52)

end

do
    local AK_BODY = 70
    local akSec, akBdy = mkSection(AfkScroll, "LOCAL AURA KILL", AK_BODY)
    
    mkToggle(akBdy, "ENABLE AURA KILL", S.afkAura, 8, function(v) S.afkAura = v end, 0.02)
    mkSlider(akBdy, "LOCAL AURA DIST", 10, 1000, S.localAuraDist, 8, function(v) S.localAuraDist = v end, 0.52)
    
    local HIP_BODY = 236
    local hipSec, hipBdy = mkSection(AfkScroll, "ISLAND HOP AURA", HIP_BODY)
    local ISLAND_LIST = {"Starter", "Jungle", "Desert", "Snow", "Shibuya", "HollowIsland", "Shinjuku", "Slime", "Academy", "Judgement", "SoulDominion", "Ninja", "Lawless", "Tower", "Boss", "Sailor"}
    
    mkMultiDD(hipBdy, "SELECT ISLANDS", ISLAND_LIST, 8, function(dict) S.afkIslands = dict end, true, 0.02, S.afkIslands)
    mkToggle(hipBdy, "ENABLE ISLAND HOP", S.afkIslandHop, 28, function(v) S.afkIslandHop = v end, 0.52)
    mkSlider(hipBdy, "HOP DELAY (SEC)", 10, 600, S.afkHopDelay, 70, function(v) S.afkHopDelay = v end, 0.02)
    
    AfkHopTimerLabel = mkLbl(hipBdy, "Next Hop: --", 11, C.Gray, true, Enum.TextXAlignment.Center, 3)
    AfkHopTimerLabel.Size = UDim2.new(0.46, 0, 0, 34); AfkHopTimerLabel.Position = UDim2.new(0.52, 4, 0, 70)
    AfkHopTimerLabel.BackgroundColor3 = C.Card
    AfkHopTimerLabel.BackgroundTransparency = 0
    rnd(AfkHopTimerLabel, 5)
    
    mkToggle(hipBdy, "HOP WHEN CLEARED", S.afkHopWhenCleared, 112, function(v) S.afkHopWhenCleared = v end, 0.02)
    mkDD(hipBdy, "ATTACK MODE", {"Kill Aura", "Single Target"}, 154, function(val) S.islandHopMode = val end, false, 0.02)
    mkSlider(hipBdy, "HOP AURA DIST", 10, 1000, S.afkIslandHopDist, 174, function(v) S.afkIslandHopDist = v end, 0.52)
end

do
    local ST_BODY = 140
    local stSec, stBdy = mkSection(MiscScroll, "AUTO STATS", ST_BODY)
    
    mkToggle(stBdy, "AUTO MELEE", false, 8, function(v) S.stats.Melee = v end, 0.02)
    mkToggle(stBdy, "AUTO SWORD", false, 50, function(v) S.stats.Sword = v end, 0.02)
    
    mkToggle(stBdy, "AUTO DEFENSE", false, 8, function(v) S.stats.Defense = v end, 0.52)
    mkToggle(stBdy, "AUTO DEVIL FRUIT", false, 50, function(v) S.stats.DevilFruit = v end, 0.52)
    
    mkTextBox(stBdy, "Add Amount (e.g. 1)", 92, function(t) S.statAmount = tonumber(t) or 1 end, 0.02)
    
    local sBtn = Instance.new("TextButton")
    sBtn.Size = UDim2.new(0.46, 0, 0, 34); sBtn.Position = UDim2.new(0.52, 4, 0, 92); sBtn.BackgroundColor3 = C.BtnBG; sBtn.Text = "ENABLE AUTO STATS"; sBtn.TextColor3 = C.Gray; sBtn.Font = Enum.Font.GothamBold; sBtn.TextSize = 10; sBtn.BorderSizePixel = 0; sBtn.ZIndex = 3; sBtn.Parent = stBdy; rnd(sBtn, 5)
    
    sBtn.Activated:Connect(function()
        S.AutoStatsActive = not S.AutoStatsActive
        if S.AutoStatsActive then
            sBtn.Text = "DISABLE AUTO STATS"
            tw(sBtn, {BackgroundColor3 = Color3.fromRGB(20,48,95), TextColor3 = C.Cyan})
        else
            sBtn.Text = "ENABLE AUTO STATS"
            tw(sBtn, {BackgroundColor3 = C.BtnBG, TextColor3 = C.Gray})
        end
    end)
end

do
    local M_BODY = 220
    local mSec, mBdy = mkSection(MiscScroll, "CHARACTER", M_BODY)

    mkToggle(mBdy, "WALKSPEED", S.misc.wsEnabled, 8, function(v) 
        S.misc.wsEnabled = v 
        if not v then 
            local char = LocalPlayer.Character
            local hum = char and char:FindFirstChild("Humanoid")
            if hum then hum.WalkSpeed = 16 end
        end
    end, 0.02)
    mkSlider(mBdy, "SPEED", 16, 200, S.misc.ws, 50, function(v) S.misc.ws = v end, 0.02)
    
    mkToggle(mBdy, "JUMPPOWER", S.misc.jpEnabled, 92, function(v) 
        S.misc.jpEnabled = v 
        if not v then
            local char = LocalPlayer.Character
            local hum = char and char:FindFirstChild("Humanoid")
            if hum then hum.UseJumpPower = true; hum.JumpPower = 50 end
        end
    end, 0.02)
    mkSlider(mBdy, "POWER", 50, 300, S.misc.jp, 134, function(v) S.misc.jp = v end, 0.02)

    mkToggle(mBdy, "NOCLIP", S.misc.noclip, 8, function(v) 
        S.misc.noclip = v 
        if not v and LocalPlayer.Character then
            for _, part in pairs(LocalPlayer.Character:GetDescendants()) do
                if part:IsA("BasePart") then part.CanCollide = true end
            end
        end
    end, 0.52)
    
    mkToggle(mBdy, "INFINITE JUMP", S.misc.infjump, 50, function(v) S.misc.infjump = v end, 0.52)
    
    mkToggle(mBdy, "ANTI RAGDOLL", S.misc.antiRagdoll, 92, function(v) 
        S.misc.antiRagdoll = v 
        if not v and LocalPlayer.Character then
            local hum = LocalPlayer.Character:FindFirstChild("Humanoid")
            if hum then
                hum:SetStateEnabled(Enum.HumanoidStateType.Ragdoll, true)
                hum:SetStateEnabled(Enum.HumanoidStateType.FallingDown, true)
            end
        end
    end, 0.52)

    mkToggle(mBdy, "AUTO OBS HAKI", S.autoObservation, 134, function(v) S.autoObservation = v end, 0.52)
end

do
    local CH_BODY = 70
    local chSec, chBdy = mkSection(ShopScroll, "INVENTORY", CH_BODY)
    
    local CHEST_LIST = {"Common Chest", "Rare Chest", "Epic Chest", "Legendary Chest", "Mythical Chest", "Secret Chest"}
    mkMultiDD(chBdy, "SELECT CHESTS", CHEST_LIST, 8, function(dict) S.chestsToOpen = dict end, true, 0.02)
    mkToggle(chBdy, "AUTO OPEN CHESTS", S.autoOpenChests, 28, function(v) S.autoOpenChests = v end, 0.52)
end

local RaceLabel = nil
do
    local RR_BODY = 100
    local rrSec, rrBdy = mkSection(ShopScroll, "RACE REROLL", RR_BODY)
    
    local RACE_LIST = {"Swordblessed", "Galevorn", "Sunborn", "Servant", "Kitsune", "Slime", "Leviathan", "Oni", "Hollow", "Shinigami", "Shadowborn", "Player", "Vessel", "Limitless", "Vampire", "Demon", "Orc", "Fishman", "Mink", "Skypea", "Human"}
    mkMultiDD(rrBdy, "TARGET RACES", RACE_LIST, 8, function(dict) S.targetRaces = dict end, true, 0.02, S.targetRaces)
    
    mkToggle(rrBdy, "AUTO RACE REROLL", S.autoRaceReroll, 28, function(v) S.autoRaceReroll = v end, 0.52)
    
    RaceLabel = mkLbl(rrBdy, "Current Race: Loading...", 10, C.Cyan, true, Enum.TextXAlignment.Center, 3)
    RaceLabel.Size = UDim2.new(0.46, 0, 0, 20); RaceLabel.Position = UDim2.new(0.52, 4, 0, 70)
end

local ClanLabel = nil
do
    local CR_BODY = 100
    local crSec, crBdy = mkSection(ShopScroll, "CLAN REROLL", CR_BODY)
    
    local CLAN_LIST = {"Sasaki", "Raikage", "Zoldyck", "Mugetsu", "Monarch", "Yamato", "Voldigoat", "Pride", "Espada", "Alter"}
    mkMultiDD(crBdy, "TARGET CLANS", CLAN_LIST, 8, function(dict) S.targetClans = dict end, true, 0.02, S.targetClans)
    
    mkToggle(crBdy, "AUTO CLAN REROLL", S.autoClanReroll, 28, function(v) S.autoClanReroll = v end, 0.52)
    
    ClanLabel = mkLbl(crBdy, "Current Clan: Loading...", 10, C.Cyan, true, Enum.TextXAlignment.Center, 3)
    ClanLabel.Size = UDim2.new(0.46, 0, 0, 20); ClanLabel.Position = UDim2.new(0.52, 4, 0, 70)
end

do
    local MB_BODY = 100
    local mbSec, mbBdy = mkSection(ShopScroll, "WANDERING MERCHANT", MB_BODY)
    
    local MERCHANT_ITEMS = {"Dungeon Key", "Boss Key", "Haki Color Reroll", "Race Reroll", "Rush Key", "Passive Shard", "Trait Reroll", "Clan Reroll", "Cosmetic Crate", "Abyssal Empress"}
    mkMultiDD(mbBdy, "MERCHANT ITEMS", MERCHANT_ITEMS, 8, function(dict) S.buyMerchantItems = dict end, true, 0.02, S.buyMerchantItems)
    mkToggle(mbBdy, "AUTO BUY MERCHANT", S.autoBuyMerchant, 28, function(v) S.autoBuyMerchant = v end, 0.52)
end

do
    local SE_BODY = 154
    local seSec, seBdy = mkSection(SettingScroll, "CONFIGURATIONS", SE_BODY)

    local cfgName = ""
    local configFiles = {}
    if listfiles and isfolder("JomHUB_Configs") then
        for _, file in ipairs(listfiles("JomHUB_Configs")) do
            local name = file:match("([^/\\]+)%.json$")
            if name then table.insert(configFiles, name) end
        end
    end

    local cfgBox = nil
    mkDD(seBdy, "SELECT CONFIG", configFiles, 8, function(val) cfgName = val; if cfgBox then cfgBox.Text = val end end, false, 0.02)
    cfgBox = mkTextBox(seBdy, "Config Name...", 28, function(t) cfgName = t end, 0.52)
    
    local saveBtn = nil
    saveBtn = mkBtn(seBdy, "SAVE CONFIG", 70, function()
        if cfgName ~= "" and writefile then
            local success, data = pcall(function() return HttpService:JSONEncode(S) end)
            if success then
                if not isfolder("JomHUB_Configs") then pcall(makefolder, "JomHUB_Configs") end
                pcall(writefile, "JomHUB_Configs/"..cfgName..".json", data)
                if saveBtn then saveBtn.Text = "SAVED!"; task.delay(2, function() if saveBtn then saveBtn.Text = "SAVE CONFIG" end end) end
                UpdateStatus("CONFIG SAVED SUCCESSFULLY!", C.Green)
            else
                UpdateStatus("CONFIG SAVE FAILED!", Color3.fromRGB(255, 0, 0))
            end
        else
            UpdateStatus("ENTER CONFIG NAME!", Color3.fromRGB(255, 170, 0))
        end
    end, 0.02)
    
    mkBtn(seBdy, "LOAD CONFIG", 112, function()
        if cfgName ~= "" and readfile and isfile("JomHUB_Configs/"..cfgName..".json") then
            local success, data = pcall(function() return HttpService:JSONDecode(readfile("JomHUB_Configs/"..cfgName..".json")) end)
            if success and type(data) == "table" then
                mergeConfig(S, data)
                RefreshUI()
                UpdateStatus("CONFIG LOADED SUCCESSFULLY!", C.Green)
            end
        end
    end, 0.02)
    
    mkBtn(seBdy, "SET AUTO LOAD", 70, function()
        if cfgName ~= "" and writefile then 
            if not isfolder("JomHUB_Configs") then pcall(makefolder, "JomHUB_Configs") end
            writefile("JomHUB_Configs/SailorPiece_autoload.txt", cfgName) 
        end
    end, 0.52)
    
    mkBtn(seBdy, "CLEAR AUTO LOAD", 112, function()
        if writefile then 
            if not isfolder("JomHUB_Configs") then pcall(makefolder, "JomHUB_Configs") end
            writefile("JomHUB_Configs/SailorPiece_autoload.txt", "") 
        end
    end, 0.52)
end

-- ============================================================
-- LOADING SEQUENCE
-- ============================================================
task.spawn(function()
    local steps = {
        {t = "Hooking Game Engine...", p = 0.3, d = 0.6},
        {t = "LOADING ASSETS...", p = 0.6, d = 0.7},
        {t = "Bypassing Anti-Cheat...", p = 0.85, d = 0.6},
        {t = "Initializing UI...", p = 1, d = 0.5}
    }
    for _, s in ipairs(steps) do
        if LoadSub then LoadSub.Text = s.t end
        if LoadBarFill then tw(LoadBarFill, {Size = UDim2.new(s.p, 0, 1, 0)}, s.d) end
        task.wait(s.d)
    end
    if LoadSub then LoadSub.Text = "Completed!" end
    task.wait(0.3)
    if LoadFrame then
        tw(LoadFrame, {BackgroundTransparency = 1}, 0.5)
        tw(LoadStroke, {Transparency = 1}, 0.5)
        tw(LoadTitle, {TextTransparency = 1}, 0.5)
        tw(LoadSub, {TextTransparency = 1}, 0.5)
        tw(LoadBarBg, {BackgroundTransparency = 1}, 0.5)
        tw(LoadBarFill, {BackgroundTransparency = 1}, 0.5)
        task.wait(0.5)
        LoadFrame:Destroy()
    end
    
    if TGlow then TGlow.Visible = true end
    if TGlowInner then TGlowInner.Visible = true end
    if TBtn then TBtn.Visible = true end
    if Win then
        Win.Visible = true
        if winScale then
            winScale.Scale = 0.8
            tw(winScale, {Scale = 1}, 0.4)
        end
    end
end)

-- ============================================================
-- GLOBAL LOOPS
-- ============================================================

local cachedBaseParts = {}
local function updateCachedParts(char)
    cachedBaseParts = {}
    for _, v in pairs(char:GetDescendants()) do
        if v:IsA("BasePart") then table.insert(cachedBaseParts, v) end
    end
end

LocalPlayer.CharacterAdded:Connect(function(char)
    updateCachedParts(char)
    char.DescendantAdded:Connect(function(v)
        if v:IsA("BasePart") then table.insert(cachedBaseParts, v) end
    end)
    char.DescendantRemoving:Connect(function(v)
        local idx = table.find(cachedBaseParts, v)
        if idx then table.remove(cachedBaseParts, idx) end
    end)
end)
if LocalPlayer.Character then updateCachedParts(LocalPlayer.Character) end

RunService.Stepped:Connect(function()
    local char = LocalPlayer.Character
    if char then
        local isFarming = S.misc.noclip or S.AutoFarmActive or S.AutoQuestActive or S.autoHuntBoss or S.autoDungeon or S.autoFarmItems or S.auto25Bosses or S.autoHogyoku or S.autoDungeonDiscovery
        if isFarming then 
            for i = 1, #cachedBaseParts do
                local p = cachedBaseParts[i]
                if p and p.CanCollide then p.CanCollide = false end
            end
        else
            -- ANTI-STUCK FAILSAFE
            local hrp = char:FindFirstChild("HumanoidRootPart")
            if hrp then for _,v in ipairs(hrp:GetChildren()) do if v.Name == "JomTeleportBV" or v.Name == "JomFarmBV" then v:Destroy() end end end
            if char:GetAttribute("IsStriking") then char:SetAttribute("IsStriking", false) end
        end
        local hum = char:FindFirstChild("Humanoid")
        if hum then
            if S.misc.wsEnabled then hum.WalkSpeed = S.misc.ws end
            if S.misc.jpEnabled then hum.UseJumpPower = true; hum.JumpPower = S.misc.jp end
            
            if S.misc.antiRagdoll then 
                hum:SetStateEnabled(Enum.HumanoidStateType.Ragdoll, false)
                hum:SetStateEnabled(Enum.HumanoidStateType.FallingDown, false)
                if hum.PlatformStand then hum.PlatformStand = false end
            end
        end
    end
end)

UserInputService.JumpRequest:Connect(function()
    if S.misc.infjump then 
        local char = LocalPlayer.Character
        local hum = char and char:FindFirstChildOfClass("Humanoid")
        if hum then hum:ChangeState("Jumping") end
    end
end)

LocalPlayer.Idled:Connect(function()
    VirtualUser:CaptureController()
    VirtualUser:ClickButton2(Vector2.new())
end)

local function executePortalTeleport(portalName)
    if TeleportToPortal then 
        pcall(function() TeleportToPortal:FireServer(portalName) end) 
    end
end

LocalPlayer.CharacterAdded:Connect(function(char)
    HakiActive = false
    HakiCooldown = 0
end)

task.spawn(function()
    while _G.JomHubRunning do
        local char = LocalPlayer.Character
        local hum = char and char:FindFirstChild("Humanoid")
        if S.autoObservation and hum and hum.Health > 0 and ObservationHakiRemote then
            if not HakiActive and tick() >= HakiCooldown then
                pcall(function() ObservationHakiRemote:FireServer("Toggle") end)
                task.wait(1)
            end
        end
        task.wait(0.5)
    end
end)

-- // SERVER NOTIFICATION LISTENER (Spawn Sync) //
if ShowNotification then
    ShowNotification.OnClientEvent:Connect(function(title, data)
        if type(data) == "table" and data.message then
            local msg = string.lower(data.message)
            if string.find(msg, "checkpoint changed") or string.find(msg, "already at this checkpoint") then
                if S.lastTargetedCrystalPos then
                    S.claimedSpawns[S.lastTargetedCrystalPos] = true
                end
                    elseif string.find(msg, "piece") then
                        if S.currentDungeonIsland and S.currentDungeonIsland ~= "" then
                            S.checkedDungeonPieces[S.currentDungeonIsland] = true
                        end
                    elseif string.find(msg, "hogyoku fragment collected") or string.find(msg, "quest in progress") or string.find(msg, "completed") then
                        if S.currentHogyokuPiece and S.currentHogyokuPiece ~= "" then
                            S.checkedHogyokuPieces[S.currentHogyokuPiece] = true
                        end
            end
        end
    end)
end

-- // SMART TELEPORT SYSTEM //
local function SmartTeleport(targetCFrame)
    local char = LocalPlayer.Character
    local hrp = char and char:FindFirstChild("HumanoidRootPart")
    local hum = char and char:FindFirstChild("Humanoid")
    if not hrp or not hum or hum.Health <= 0 then return end
    
    local dist = (hrp.Position - targetCFrame.Position).Magnitude
    if dist < 50 then
        hrp.CFrame = targetCFrame
        return
    end
    
    if S.teleportMode == "Instant" then
        if dist > 500 then
            pcall(function() LocalPlayer:RequestStreamAroundAsync(targetCFrame.Position) end)
            task.wait(0.1)
        end
        hrp.CFrame = targetCFrame
        return
    end
    
    local speed = 150 -- Boosted speed for smooth Tweening
    local timeToReach = math.max(0.1, dist / speed)
    
    local fbv = hrp:FindFirstChild("JomFarmBV")
    if fbv then fbv:Destroy() end
    
    local bv = hrp:FindFirstChild("JomTeleportBV")
    if not bv then
        bv = Instance.new("BodyVelocity")
        bv.Name = "JomTeleportBV"; bv.Velocity = Vector3.zero; bv.MaxForce = Vector3.new(math.huge, math.huge, math.huge); bv.Parent = hrp
    end
    
    local nc = RunService.Stepped:Connect(function()
        if char then for _, v in pairs(char:GetDescendants()) do if v:IsA("BasePart") then v.CanCollide = false end end end
    end)
    
    local tweenInfo = TweenInfo.new(timeToReach, Enum.EasingStyle.Linear)
    local tween = TweenService:Create(hrp, tweenInfo, {CFrame = targetCFrame})
    
    local conn
    conn = RunService.Heartbeat:Connect(function()
        local isFarming = _G.JomHubRunning and char and hrp and hum and hum.Health > 0 and (S.AutoFarmActive or S.AutoQuestActive or S.autoDungeonDiscovery or S.auto25Bosses or S.autoHuntBoss or S.autoHuntBossNoHop or S.autoHogyoku or S.afkAura or S.afkIslandHop or S.autoFarmItems)
        if not isFarming then
            tween:Cancel()
        end
    end)
    
    tween:Play()
    tween.Completed:Wait()
    
    if conn then conn:Disconnect() end
    if nc then nc:Disconnect() end
    if bv then bv:Destroy() end
end

local function getFarmPosition(targetRoot, baseDistance, posType)
    local enemyPos = targetRoot.Position
    local totalDist = baseDistance -- Pure 1:1 calculation without invisible boss padding
    
    local tpPos = enemyPos
    if posType == "Above" then 
        tpPos = enemyPos + Vector3.new(0, totalDist, 0)
        return CFrame.new(tpPos) * CFrame.Angles(math.rad(-90), 0, 0)
    elseif posType == "Below" then 
        tpPos = enemyPos + Vector3.new(0, -totalDist, 0)
        return CFrame.new(tpPos) * CFrame.Angles(math.rad(90), 0, 0)
    else
        local lookVec = targetRoot.CFrame.LookVector
        local angle = math.atan2(-lookVec.X, -lookVec.Z)
        local flatCF = CFrame.new(enemyPos) * CFrame.Angles(0, angle, 0)
        if posType == "Front" then tpPos = (flatCF * CFrame.new(0, 0, -totalDist)).Position
        elseif posType == "Behind" then tpPos = (flatCF * CFrame.new(0, 0, totalDist)).Position
        elseif posType == "Right" then tpPos = (flatCF * CFrame.new(totalDist, 0, 0)).Position
        elseif posType == "Left" then tpPos = (flatCF * CFrame.new(-totalDist, 0, 0)).Position
        else tpPos = enemyPos + Vector3.new(0, totalDist, 0) end
    end
    return CFrame.lookAt(tpPos, enemyPos)
end

-- 1. AUTO FARM LOOP
local MELEE_LIST = {"Anos", "King of heroes", "Strongest in History", "Strongest of Today", "Madoka", "Alucard", "Qin Shi", "Curse king", "Limitless sorcerer", "Yuji", "Combat", "Corrupted Excalibur", "Strongest Shinobi", "Blessed Maiden", "Saber Alter", "Fists", "Brass Knuckles", "Fighting Style", "Gauntlets", "Dragon Gauntlets"}
local SWORD_LIST = {"Shadow Monarch", "Sin of pride", "Rimuru", "Shadow", "Ichigo", "Manipulator", "Ragna", "Solo hunter", "Excalibur", "Dark Blade", "Katana", "Gryphon", "True Manipulator", "Atomic", "Abyssal Empress", "Sword", "KatanaSword", "Cutlass", "Longsword", "Dual Katana"}
local FRUIT_LIST = {"Light", "Quake", "Flame", "Bomb", "Invisible"}

local function getWeaponsToUse()
    local weapons = {}
    local bp = LocalPlayer:FindFirstChild("Backpack")
    local char = LocalPlayer.Character
    local function checkList(list)
        for _, name in ipairs(list) do
            local w = (char and char:FindFirstChild(name)) or (bp and bp:FindFirstChild(name))
            if w then return w end
        end
        return nil
    end
    if S.autoMelee then local w = checkList(MELEE_LIST); if w then table.insert(weapons, w) end end
    if S.autoSword then local w = checkList(SWORD_LIST); if w then table.insert(weapons, w) end end
    if S.autoFruit then local w = checkList(FRUIT_LIST); if w then table.insert(weapons, w) end end
    return weapons
end

local currentFarmTarget = nil
local currentTargetCrystal = nil

local function isMobTarget(npc, tMob, manualTargets)
    if not npc or not npc.Name then return false end
    local cleanName = npc.Name:gsub("%d+$", ""):match("^%s*(.-)%s*$")
    local noSpaceName = cleanName and string.gsub(cleanName, "%s+", "") or ""
    
    local function checkMatch(target)
        if not target or target == "" then return false end
        local noSpaceTarget = string.gsub(target, "%s+", "")
        if string.lower(noSpaceName) == string.lower(noSpaceTarget) then return true end
        
        if string.find(string.lower(noSpaceTarget), "boss") then
            local strippedTarget = string.gsub(string.lower(noSpaceTarget), "boss", "")
            if string.lower(noSpaceName) == strippedTarget then
                local hum = npc:FindFirstChild("Humanoid")
                if hum and hum.MaxHealth > 5000 then return true end
            end
        end
        return false
    end
    
    if checkMatch(tMob) then return true end
    
    if manualTargets and next(manualTargets) then
        for k, v in pairs(manualTargets) do
            if v and checkMatch(k) then return true end
        end
    end
    return false
end

local function getPortalForMob(mobName)
    if not mobName or mobName == "" then return nil end
    local cleanName = string.gsub(mobName, "%d+$", "")
    local ln = string.gsub(string.lower(cleanName), "%s+", "")
    
    local PORTAL_MAP = {
        thief = "Starter", thiefboss = "Starter",
        monkey = "Jungle", monkeyboss = "Jungle",
        desertbandit = "Desert", desertboss = "Desert",
        frostrogue = "Snow", winterwarden = "Snow", snowboss = "Snow", ragna = "Snow", ragnaboss = "Snow",
        jinwooboss = "Sailor", jinwoo = "Sailor", shadowboss = "Sailor", shadowmonarchboss = "Sailor",
        yujiboss = "Shibuya", sukunaboss = "Shibuya", gojoboss = "Shibuya", sorcererstudent = "Shibuya", pandasorcerer = "Shibuya", sorcerer = "Shibuya", pandaminiboss = "Shibuya", strongestoftodayboss = "Shibuya",        
        hollow = "HollowIsland", aizenboss = "HollowIsland", aizen = "HollowIsland",
        trueaizenboss = "SoulDominion",
        curse = "Shinjuku", strongsorcerer = "Shinjuku", strongestinhistoryboss = "Shinjuku", strongestoftodayboss = "Shinjuku",
        slimewarrior = "Slime", slime = "Slime", madokaboss = "Valentine", madoka = "Valentine", rimuruboss = "Slime",
        academyteacher = "Academy", anosboss = "Academy",
        swordsman = "Judgement", yamatoboss = "Judgement", yamato = "Judgement", alucardboss = "Sailor", alucard = "Sailor", qinshiboss = "Boss",
        quincy = "SoulDominion", ichigoboss = "Boss",
        saberboss = "Boss", saber = "Boss", gilgameshboss = "Boss", escanorboss = "Sailor",
        blessedmaidenboss = "Boss", saberalterboss = "Boss", atomicboss = "Lawless", atomic = "Lawless", strongestshinobiboss = "Ninja",
        ninja = "Ninja", arenafighter = "Lawless"
    }
    
    if PORTAL_MAP[ln] then return PORTAL_MAP[ln] end
    local lnNoBoss = string.gsub(ln, "boss", "")
    if PORTAL_MAP[lnNoBoss] then return PORTAL_MAP[lnNoBoss] end
    
    for _, tier in ipairs(PROGRESSION_MAP) do
        if string.lower(tier.Mob) == ln or string.lower(tier.Mob) == lnNoBoss then 
            return tier.Portal 
        end
    end
    return nil
end

local function checkBossAlive(bName)
    local cName = "TimedBossSpawn_" .. bName .. "_Container"
    if bName == "YamatoBoss" then cName = "TimedBossSpawn_Yamato_Container" end
    local cont = Workspace:FindFirstChild(cName)
    if cont then
        local bb = cont:FindFirstChild("BossTimerBillboard", true)
        local lbl = bb and bb:FindFirstChild("Timer", true)
        if lbl and lbl:IsA("TextLabel") then
            local txt = string.lower(lbl.Text)
            if string.find(txt, "alive") or string.find(txt, "spawn") then return true end
            
            local h, m, s = string.match(txt, "(%d+):(%d+):(%d+)")
            local m2, s2 = string.match(txt, "(%d+):(%d+)")
            local s_only = string.match(txt, "^(%d+)$")
            if h or m2 or s_only then return false end
        end
    end
    if NPCs then
        for _, npc in ipairs(NPCs:GetChildren()) do
            local cleanBName = string.gsub(string.lower(bName), "boss", "")
            local noSpaceNPC = string.gsub(npc.Name, "%s+", "")
            
            if string.find(string.lower(noSpaceNPC), string.lower(bName)) then
                local hum = npc:FindFirstChild("Humanoid")
                if hum and hum.Health > 0 then return true end
            elseif string.find(string.lower(noSpaceNPC), cleanBName) then
                local hum = npc:FindFirstChild("Humanoid")
                if hum and hum.Health > 0 and hum.MaxHealth > 5000 then return true end
            end
        end
    end
    if BossTimersCache[bName] and (BossTimersCache[bName].isAlive or BossTimersCache[bName].state == "SPAWNED") then return true end
    return false
end

local function getPhysicalTimer(bName)
    local cleanBName = string.gsub(bName, "Boss", "")
    local cName = "TimedBossSpawn_" .. bName .. "_Container"
    local cName2 = "TimedBossSpawn_" .. cleanBName .. "_Container"
    local cont = Workspace:FindFirstChild(cName) or Workspace:FindFirstChild(cName2)
    if not cont and bName == "YamatoBoss" then cont = Workspace:FindFirstChild("TimedBossSpawn_Yamato_Container") end
    
    if cont then
        local bb = cont:FindFirstChild("BossTimerBillboard", true)
        if bb and bb:IsA("BillboardGui") and not bb.Enabled then return nil end
        local lbl = bb and bb:FindFirstChild("Timer", true)
        if lbl and not lbl.Visible then return nil end
        if lbl and lbl:IsA("TextLabel") then
            local txt = string.lower(lbl.Text)
            if string.find(txt, "alive") or string.find(txt, "spawn") then return 0 end
            local h, m, s = string.match(txt, "(%d+):(%d+):(%d+)")
            if h and m and s then return (tonumber(h)*3600) + (tonumber(m)*60) + tonumber(s) end
            local m2, s2 = string.match(txt, "(%d+):(%d+)")
            if m2 and s2 then return (tonumber(m2)*60) + tonumber(s2) end
            local s_only = string.match(txt, "^(%d+)$")
            if s_only then return tonumber(s_only) end
        end
    end
    return nil
end

task.spawn(function()
    
    local function getCurrentLevel()
        local leaderstats = LocalPlayer:FindFirstChild("leaderstats")
        if leaderstats and leaderstats:FindFirstChild("Level") then
            return leaderstats.Level.Value
        end
        -- Fallback if level is stored in attributes or data folder
        if LocalPlayer:GetAttribute("Level") then return LocalPlayer:GetAttribute("Level") end
        local data = LocalPlayer:FindFirstChild("Data")
        if data and data:FindFirstChild("Level") then return data.Level.Value end
        return 1
    end

    local function getSortedKeys(dict)
        local keys = {}
        if dict then
            for k, v in pairs(dict) do if v then table.insert(keys, k) end end
            table.sort(keys)
        end
        return keys
    end

    local LocallyDeadBosses = {}

    local PRIORITY_BOSS_LIST = {
        -- HIGH PRIORITY: Spawnable Bosses
        "JinwooBoss", "AlucardBoss", "YujiBoss", "GojoBoss", "SukunaBoss", "AizenBoss", "MadokaBoss", "SaberBoss", "QinShiBoss", "IchigoBoss", "GilgameshBoss", 
        "StrongestofTodayBoss", "StrongestinHistoryBoss", "RimuruBoss", "AnosBoss", "EscanorBoss", "TrueAizenBoss", "YamatoBoss",
        "StrongestShinobiBoss", "BlessedMaidenBoss", "SaberAlterBoss", "AtomicBoss",
        
        -- LOW PRIORITY: Mob / Minor Bosses
        "DesertBoss", "MonkeyBoss", "PandaMiniBoss", "RagnaBoss", "ShadowBoss", "ShadowMonarchBoss", "SnowBoss", "ThiefBoss"
    }

    local function getAliveBoss()
        for _, bName in ipairs(PRIORITY_BOSS_LIST) do
            if checkBossAlive(bName) then 
                return true, bName
            end
        end
        return false, nil
    end

    while _G.JomHubRunning do
        local char = LocalPlayer.Character
        local hrp = char and char:FindFirstChild("HumanoidRootPart")
        
        local isInDungeonEnv = (game.PlaceId == 75159314259063 or game.PlaceId == 99684056491472 or game.PlaceId == 123955125827131 or game.PlaceId == 96767841099256 or game.PlaceId == 138368689293913)
        local isFarming = S.AutoFarmActive and not (S.autoDungeon and isInDungeonEnv)
        local targetMob = ""
        local manualTargets = S.selectedMobs
        local targetQuest = S.questName

        if S.autoFarmItems then
            manualTargets = nil
            local activeItems = {}
            local seenTargets = {}
            
            local function addItems(dict)
                for _, k in ipairs(getSortedKeys(dict)) do
                    local tLogic = ITEM_TARGETS[k]
                    if tLogic and not seenTargets[tLogic] then
                        seenTargets[tLogic] = true
                        table.insert(activeItems, k)
                    end
                end
            end
            
            addItems(S.farmSecret)
            addItems(S.farmMythical)
            addItems(S.farmLegendary)
            addItems(S.farmEpic)
            addItems(S.farmRare)
            addItems(S.farmCommon)

            if #activeItems > 0 then
                local cIdx = char:GetAttribute("ItemFarmIdx") or 1
                if cIdx > #activeItems then cIdx = 1; char:SetAttribute("ItemFarmIdx", cIdx) end
                
                                local currentItem = activeItems[cIdx]
                local targetLogic = ITEM_TARGETS[currentItem]
                
                local startTime = char:GetAttribute("ItemFarmTime")
                if not startTime then 
                    startTime = tick()
                    char:SetAttribute("ItemFarmTime", startTime)
                end
                
                if tick() - startTime >= S.itemFarmDuration then
                    char:SetAttribute("ItemFarmIdx", cIdx + 1)
                    char:SetAttribute("ItemFarmTime", tick())
                    UpdateStatus("ITEM FARM: Time limit reached! Switching item...", C.Cyan)
                    task.wait(2.5)
                    continue
                end
                
                if targetLogic == "AutoLevel" then
                    local myLevel = getCurrentLevel()
                    local bestTier = PROGRESSION_MAP[1]
                    for _, tier in ipairs(PROGRESSION_MAP) do
                        if myLevel >= tier.Level then bestTier = tier else break end
                    end
                    if S.lastPortal ~= bestTier.Portal and bestTier.Portal then
                        UpdateStatus("ITEM FARM: Teleporting to " .. bestTier.Portal .. "...", C.Cyan)
                        executePortalTeleport(bestTier.Portal)
                        S.lastPortal = bestTier.Portal
                        task.wait(3.5)
                        continue
                    end
                    targetMob = bestTier.Mob
                    if S.itemFarmTakeQuests then
                        if S.lastAutoQuest ~= bestTier.Quest then
                            if S.lastAutoQuest ~= "" and QuestAbandon then
                                pcall(function() QuestAbandon:FireServer("repeatable") end)
                                pcall(function() QuestAbandon:FireServer("Abandon") end)
                                pcall(function() QuestAbandon:FireServer(S.lastAutoQuest) end)
                                pcall(function() QuestAbandon:FireServer() end)
                            end
                            S.lastAutoQuest = bestTier.Quest
                            S.forceQuestAccept = true
                        end
                        targetQuest = bestTier.Quest
                        S.AutoQuestActive = true
                    else
                        targetQuest = ""
                        S.AutoQuestActive = false
                    end
                    isFarming = true
                elseif targetLogic == "AllBosses" then
                    local anyAlive, bName = getAliveBoss()
                    if anyAlive and bName then
                        targetMob = bName
                        targetQuest = ""
                        isFarming = true
                    else
                        char:SetAttribute("ItemFarmIdx", cIdx + 1)
                        char:SetAttribute("ItemFarmTime", tick())
                        UpdateStatus("ITEM FARM: No bosses alive for [" .. currentItem .. "]. Skipping...", C.Gray)
                        task.wait(1.5)
                        continue
                    end
            elseif targetLogic == "SweepSlimeToSoul" then
                local sweepMobs = {"Slime", "AcademyTeacher", "Swordsman", "Quincy"}
                local sIdx = char:GetAttribute("SweepFarmIdx") or 1
                if sIdx > #sweepMobs then sIdx = 1; char:SetAttribute("SweepFarmIdx", 1) end
                
                local cMob = sweepMobs[sIdx]
                local anyAlive = false
                
                if NPCs then
                    for _, npc in ipairs(NPCs:GetChildren()) do
                        if isMobTarget(npc, cMob, nil) then
                            local hum = npc:FindFirstChild("Humanoid")
                            if hum and hum.Health > 0 then
                                anyAlive = true
                                break
                            end
                        end
                    end
                end
                
                if not anyAlive and S.lastPortal == getPortalForMob(cMob) then
                    sIdx = sIdx < #sweepMobs and sIdx + 1 or 1
                    char:SetAttribute("SweepFarmIdx", sIdx)
                    cMob = sweepMobs[sIdx]
                end
                
                targetMob = cMob
                targetQuest = ""
                isFarming = true
                else
                    local isBoss = string.find(string.lower(targetLogic), "boss") ~= nil
                    if isBoss then
                        if checkBossAlive(targetLogic) then
                            targetMob = targetLogic
                            targetQuest = ""
                            isFarming = true
                        else 
                            char:SetAttribute("ItemFarmIdx", cIdx + 1)
                            char:SetAttribute("ItemFarmTime", tick())
                            UpdateStatus("ITEM FARM: [" .. targetLogic .. "] is dead. Skipping...", C.Gray)
                            task.wait(1.5)
                            continue
                        end
                    else
                        targetMob = targetLogic
                        targetQuest = ""
                        isFarming = true
                    end
                end
            else
                UpdateStatus("ITEM FARM: No items selected.", C.Gray)
            end
        elseif S.autoHuntBoss or S.autoHuntBossNoHop then
            local bossToKill = nil
            local anyAlive = false
            local waitToSpawn = false
            local waitingForBossName = ""
            
            local function isLocallyDead(bName)
                local deadTick = LocallyDeadBosses[bName]
                -- Ignore inactive/dead bosses for 30 minutes on this server
                if deadTick and (tick() - deadTick < 1800) then return true end
                return false
            end
            
            local physicalTimers = {}
            for bName, isSelected in pairs(S.huntBosses) do
                if isSelected then
                    if isLocallyDead(bName) then continue end
                    local t = getPhysicalTimer(bName)
                    if t then physicalTimers[bName] = t end
                end
            end
            
            local minWaitTime = math.huge
            for bName, t in pairs(physicalTimers) do
                if t == 0 then 
                    bossToKill = bName; anyAlive = true; break
                elseif t <= S.huntHopTimer and t < minWaitTime then 
                    waitToSpawn = true; waitingForBossName = bName; minWaitTime = t
                end
            end

            if not anyAlive and NPCs then
                for _, npc in ipairs(NPCs:GetChildren()) do
                    if npc:IsA("Model") then
                        for bName, isSelected in pairs(S.huntBosses) do
                            if isSelected then
                                local cleanBName = string.gsub(string.lower(bName), "boss", "")
                                local noSpaceNPC = string.gsub(npc.Name, "%s+", "")
                                
                                local isExactMatch = string.find(string.lower(noSpaceNPC), string.lower(bName)) ~= nil
                                local isFallbackMatch = string.find(string.lower(noSpaceNPC), cleanBName) ~= nil
                                
                                local hum = npc:FindFirstChild("Humanoid")
                                if hum and hum.Health > 0 then
                                    if isExactMatch then
                                        bossToKill = npc.Name
                                        anyAlive = true
                                        break
                                    elseif isFallbackMatch and hum.MaxHealth > 5000 then
                                        bossToKill = npc.Name
                                        anyAlive = true
                                        break
                                    end
                                end
                            end
                        end
                    end
                    if anyAlive then break end
                end
            end
            
            if not anyAlive and not waitToSpawn then
                for bName, isSelected in pairs(S.huntBosses) do
                    if isSelected then
                        if isLocallyDead(bName) then continue end
                        -- Prevent trusting remote cache if physical timer explicitly exists and is ticking
                        if physicalTimers[bName] and physicalTimers[bName] > 0 then continue end
                        
                        local info = BossTimersCache[bName]
                        if info then
                            if info.isAlive or info.state == "SPAWNED" then
                                bossToKill = bName; anyAlive = true; break
                            elseif info.timer and info.timer <= S.huntHopTimer and info.timer < minWaitTime then
                                waitToSpawn = true; waitingForBossName = bName; minWaitTime = info.timer
                            end
                        end
                    end
                end
            end
            
            local currentBossKey = nil
            if anyAlive then
                local bestMatch = nil
                for bName, isSelected in pairs(S.huntBosses) do
                    local cleanBName = string.gsub(string.lower(bName), "boss", "")
                    if isSelected and (string.find(string.lower(bossToKill), string.lower(bName)) or string.find(string.lower(bossToKill), cleanBName)) then
                        if not bestMatch or #bName > #bestMatch then
                            bestMatch = bName
                        end
                    end
                end
                currentBossKey = bestMatch or bossToKill
            elseif waitToSpawn then
                currentBossKey = waitingForBossName 
            end
            
            if currentBossKey then
                if char:GetAttribute("HopDelayStarted") then char:SetAttribute("HopDelayStarted", nil) end
                if S.lastBossHuntTarget ~= currentBossKey then
                    S.lastBossHuntTarget = currentBossKey
                    S.lastPortal = ""
                end
                
                if anyAlive then
                    isFarming = true
                    targetMob = bossToKill
                    manualTargets = nil
                    targetQuest = ""
                    S.AutoQuestActive = false
                end

                local foundInWorkspace = false
                if NPCs and anyAlive then
                    for _, npc in ipairs(NPCs:GetChildren()) do
                        local cleanBossToKill = string.gsub(string.lower(currentBossKey), "boss", "")
                        local noSpaceNPC = string.gsub(npc.Name, "%s+", "")
                        
                        local isExactMatch = string.find(string.lower(noSpaceNPC), string.lower(currentBossKey)) ~= nil
                        local isFallbackMatch = string.find(string.lower(noSpaceNPC), cleanBossToKill) ~= nil
                        
                        local hum = npc:FindFirstChild("Humanoid")
                        if hum and hum.Health > 0 then
                            if isExactMatch then
                                foundInWorkspace = true
                                break
                            elseif isFallbackMatch and hum.MaxHealth > 5000 then
                                foundInWorkspace = true
                                break
                            end
                        end
                    end
                end
                
                if foundInWorkspace then
                    if char then char:SetAttribute("BossLoadWaitAttempts", 0) end
                end
                
                local containerPos = nil
                local isNearContainer = false
                local cleanBName = string.gsub(currentBossKey, "Boss", "")
                local cName = "TimedBossSpawn_" .. currentBossKey .. "_Container"
                local cName2 = "TimedBossSpawn_" .. cleanBName .. "_Container"
                local cont = Workspace:FindFirstChild(cName) or Workspace:FindFirstChild(cName2)
                
                if not cont and bName == "YamatoBoss" then
                    cont = Workspace:FindFirstChild("TimedBossSpawn_Yamato_Container")
                end
                
                if cont then
                    local sp = cont:FindFirstChild("TimedBossSpawn_" .. currentBossKey) or cont
                    if sp then
                        if sp:IsA("Model") and sp.PrimaryPart then containerPos = sp:GetPivot().Position
                        elseif sp:IsA("Model") then 
                            local p = sp:FindFirstChildWhichIsA("BasePart", true)
                            if p then containerPos = p.Position end
                        elseif sp:IsA("BasePart") then containerPos = sp.Position 
                        else
                            local p = sp:FindFirstChildWhichIsA("BasePart", true)
                            if p then containerPos = p.Position end
                        end
                    end
                end
                
                if containerPos and hrp and (hrp.Position - containerPos).Magnitude < 3000 then
                    isNearContainer = true
                end
                
                if not foundInWorkspace and not isNearContainer then
                    local BOSS_PORTALS = {
                        {k = "trueaizen", v = "SoulDominion"}, {k = "aizen", v = "HollowIsland"},
                        {k = "gojo", v = "Shibuya"}, {k = "yuji", v = "Shibuya"},
                        {k = "sukuna", v = "Shibuya"}, {k = "yamato", v = "Judgement"},
                        {k = "saber", v = "Boss"}, {k = "jinwoo", v = "Sailor"},
                        {k = "alucard", v = "Sailor"}, {k = "madoka", v = "Valentine"}, {k = "ragna", v = "Snow"},
                        {k = "qinshi", v = "Boss"}, {k = "ichigo", v = "Boss"}, {k = "gilgamesh", v = "Boss"},
                        {k = "strongestoftoday", v = "Shinjuku"}, {k = "strongestinhistory", v = "Shinjuku"},
                        {k = "rimuru", v = "Slime"}, {k = "anos", v = "Academy"}, {k = "escanor", v = "Sailor"},
                        {k = "strongestshinobi", v = "Ninja"}, {k = "blessedmaiden", v = "Boss"}, {k = "saberalter", v = "Boss"}, {k = "atomic", v = "Lawless"}                    }
                    local pName = nil
                    for _, bData in ipairs(BOSS_PORTALS) do
                        if string.find(string.lower(currentBossKey), bData.k) then pName = bData.v; break end
                    end
                    
                    if pName then
                        if S.lastPortal ~= pName then
                            UpdateStatus("BOSS HUNT: Teleporting to " .. pName .. "...", C.Cyan)
                            executePortalTeleport(pName)
                            S.lastPortal = pName
                            task.wait(3)
                            continue
                        elseif not containerPos then
                            local tpAttempts = char:GetAttribute("PortalRetry_" .. pName) or 0
                            if tpAttempts < 2 then
                                char:SetAttribute("PortalRetry_" .. pName, tpAttempts + 1)
                                UpdateStatus("BOSS HUNT: Retrying teleport to " .. pName .. "...", C.Cyan)
                                executePortalTeleport(pName)
                                task.wait(3)
                                continue
                            end
                        end
                    end
                end
                
                if waitToSpawn then
                    local timeLeft = physicalTimers[currentBossKey] or (BossTimersCache[currentBossKey] and BossTimersCache[currentBossKey].timer) or S.huntHopTimer
                    local displayTime = tonumber(timeLeft) or 0
                    
                    UpdateStatus("BOSS HUNT: Waiting for " .. currentBossKey .. " (" .. displayTime .. "s)...", C.Cyan)
                    
                    if displayTime <= 0 then
                        local waitAttempts = char:GetAttribute("BossLoadWaitAttempts") or 0
                        if waitAttempts > 20 then
                            UpdateStatus("BOSS HUNT: " .. currentBossKey .. " appears inactive. Ignoring...", C.Gray)
                            LocallyDeadBosses[currentBossKey] = tick()
                            if BossTimersCache[currentBossKey] then
                                BossTimersCache[currentBossKey].isAlive = false
                                BossTimersCache[currentBossKey].state = "DEAD"
                                BossTimersCache[currentBossKey].timer = 3600
                            end
                            char:SetAttribute("BossLoadWaitAttempts", 0)
                            task.wait(1)
                            continue
                        else
                            char:SetAttribute("BossLoadWaitAttempts", waitAttempts + 1)
                        end
                    else
                        char:SetAttribute("BossLoadWaitAttempts", 0)
                    end
                    if containerPos and hrp then
                        if (hrp.Position - containerPos).Magnitude > 50 then
                            SmartTeleport(CFrame.new(containerPos + Vector3.new(0, 15, 0)))
                        else
                            hrp.Velocity = Vector3.zero
                            task.wait(1)
                        end
                    else
                        task.wait(1)
                    end
                    continue
                end
                
                if anyAlive and not foundInWorkspace then
                    if containerPos and hrp and (hrp.Position - containerPos).Magnitude < 150 then
                        local waitAttempts = char:GetAttribute("BossLoadWaitAttempts") or 0
                        if waitAttempts > 5 then
                            UpdateStatus("BOSS HUNT: " .. currentBossKey .. " appears dead. Updating cache...", C.Gray)
                            char:SetAttribute("DeadBoss_" .. currentBossKey, tick())
                            if BossTimersCache[currentBossKey] then
                                BossTimersCache[currentBossKey].isAlive = false
                                BossTimersCache[currentBossKey].state = "DEAD"
                                BossTimersCache[currentBossKey].timer = 3600
                            end
                            char:SetAttribute("BossLoadWaitAttempts", 0)
                            task.wait(1)
                            continue
                        else
                            UpdateStatus("BOSS HUNT: Waiting for " .. currentBossKey .. " to load...", C.Cyan)
                            char:SetAttribute("BossLoadWaitAttempts", waitAttempts + 1)
                            if (hrp.Position - containerPos).Magnitude > 20 then
                                SmartTeleport(CFrame.new(containerPos + Vector3.new(0, 15, 0)))
                            else
                                hrp.Velocity = Vector3.zero
                            end
                            task.wait(1)
                            continue
                        end
                    else
                        UpdateStatus("BOSS HUNT: Moving to " .. currentBossKey .. " spawner...", C.Cyan)
                        if containerPos and hrp then
                            SmartTeleport(CFrame.new(containerPos + Vector3.new(0, 15, 0)))
                        else
                            task.wait(1)
                        end
                        char:SetAttribute("BossLoadWaitAttempts", 0)
                        continue
                    end
                end
            elseif next(S.huntBosses) then
                if S.autoHuntBoss then
                    if not char:GetAttribute("HopDelayStarted") then
                        char:SetAttribute("HopDelayStarted", tick())
                    end
                    
                    if tick() - char:GetAttribute("HopDelayStarted") < 8 then
                        UpdateStatus("BOSS HUNT: Scanning server for bosses...", C.Cyan)
                        task.wait(1)
                        continue
                    end
                    
                    UpdateStatus("BOSS HUNT: No selected bosses alive. Hopping server...", C.Gray)
                    task.wait(1)
                    ServerHop()
                    task.wait(10)
                else
                    UpdateStatus("BOSS HUNT: No selected bosses alive. Waiting...", C.Gray)
                    task.wait(1)
                end
                continue
            end
        elseif S.autoSummonPity or S.autoHopPity then
            isFarming = true
            local pityCount = _G.JomHub_BossPity or 0
            local targetBossName = ""
            local shouldHop = false
            
            if pityCount >= 24 then
                targetBossName = S.pitySummonBoss or "SaberBoss"
            else
                if S.autoHopPity then
                    local aliveWorldBosses = {}
                    for bName, info in pairs(BossTimersCache) do
                        if info.isAlive or info.state == "SPAWNED" then
                            table.insert(aliveWorldBosses, bName)
                        end
                    end
                    
                    if #aliveWorldBosses == 0 then
                        local WORLDBOSSES = {"GojoBoss", "YujiBoss", "YamatoBoss", "AlucardBoss", "StrongestShinobiBoss", "JinwooBoss", "SukunaBoss", "AizenBoss"}
                        for _, b in ipairs(WORLDBOSSES) do
                            if checkBossAlive(b) then
                                table.insert(aliveWorldBosses, b)
                            end
                        end
                    end

                    if #aliveWorldBosses > 0 then
                        local selected = aliveWorldBosses[1]
                        for _, b in ipairs(aliveWorldBosses) do
                            if b ~= (S.pitySummonBoss or "SaberBoss") then
                                selected = b
                                break
                            end
                        end
                        targetBossName = selected
                    else
                        shouldHop = true
                    end
                else
                    local builderList = getSortedKeys(S.pityBuilderBosses)
                    if #builderList == 0 then builderList = {"SaberBoss"} end
                    
                    local foundAlive = false
                    for _, b in ipairs(builderList) do
                        if checkBossAlive(b) then targetBossName = b; foundAlive = true; break end
                    end
                    if not foundAlive then
                        local cIdx = char:GetAttribute("PityBuilderIdx") or 1
                        if cIdx > #builderList then cIdx = 1; char:SetAttribute("PityBuilderIdx", 1) end
                        targetBossName = builderList[cIdx]
                    end
                end
            end
            
            targetMob = targetBossName
            manualTargets = nil
            targetQuest = ""
            S.AutoQuestActive = false
            
            if shouldHop then
                if not char:GetAttribute("PityHopDelay") then
                    char:SetAttribute("PityHopDelay", tick())
                end
                if tick() - char:GetAttribute("PityHopDelay") > 5 then
                    UpdateStatus("AUTO HOP PITY: No world bosses alive. Hopping...", C.Orange)
                    task.wait(1)
                    ServerHop()
                    task.wait(10)
                    continue
                else
                    UpdateStatus("AUTO HOP PITY: Scanning for alive bosses...", C.Cyan)
                    task.wait(1)
                    continue
                end
            else
                char:SetAttribute("PityHopDelay", nil)
            end
            
            local targetIsland = getPortalForMob(targetBossName) or "Starter"
            if S.lastPortal ~= targetIsland then
                UpdateStatus((S.autoHopPity and "AUTO HOP PITY" or "AUTO PITY") .. ": Teleporting to " .. targetIsland .. "...", C.Cyan)
                executePortalTeleport(targetIsland)
                S.lastPortal = targetIsland
                task.wait(3)
                continue
            end
            
            local bossFound = false
            local activeBossObj = nil
            
            if NPCs then
                for _, npc in ipairs(NPCs:GetChildren()) do
                    local cleanBName = string.gsub(string.lower(targetBossName), "boss", "")
                    if string.find(string.lower(npc.Name), cleanBName) then
                        local hum = npc:FindFirstChild("Humanoid")
                        if hum and hum.Health > 0 then
                            bossFound = true
                            activeBossObj = npc
                            targetMob = npc.Name
                            break
                        end
                    end
                end
            end
            
            -- PITY TRACKING LOGIC
            if activeBossObj and not char:GetAttribute("TrackingPityFor") then
                char:SetAttribute("TrackingPityFor", activeBossObj.Name)
                local hum = activeBossObj:FindFirstChild("Humanoid")
                local conn
                conn = hum.Died:Connect(function()
                    if S.autoSummonPity or S.autoHopPity then
                        if _G.JomHub_BossPity >= 24 then
                            _G.JomHub_BossPity = 0
                            UpdateStatus("AUTO PITY: Target Boss Defeated! Pity Reset to 0/25.", C.Green)
                        else
                            _G.JomHub_BossPity = _G.JomHub_BossPity + 1
                            UpdateStatus("AUTO PITY: Builder Boss Defeated! Pity: " .. _G.JomHub_BossPity .. "/25", C.Green)
                        end
                        if PityCounterLabel then PityCounterLabel.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25" end
                        if PityCounterLabel2 then PityCounterLabel2.Text = "Current Pity: " .. _G.JomHub_BossPity .. "/25" end
                    end
                    char:SetAttribute("TrackingPityFor", nil)
                    if conn then conn:Disconnect() end
                end)
            elseif not activeBossObj then
                char:SetAttribute("TrackingPityFor", nil)
            end
            
            if not bossFound and targetBossName ~= "" then
                local cName = "TimedBossSpawn_" .. targetBossName .. "_Container"
                local cleanBName = string.gsub(targetBossName, "Boss", "")
                local cName2 = "TimedBossSpawn_" .. cleanBName .. "_Container"
                local cont = Workspace:FindFirstChild(cName) or Workspace:FindFirstChild(cName2)
                
                if not cont and targetBossName == "YamatoBoss" then
                    cont = Workspace:FindFirstChild("TimedBossSpawn_Yamato_Container")
                end
                
                local cPos = nil
                if cont then
                    local sp = cont:FindFirstChild("TimedBossSpawn_" .. targetBossName) or cont
                    if sp:IsA("Model") and sp.PrimaryPart then cPos = sp:GetPivot().Position
                    elseif sp:IsA("Model") then 
                        local p = sp:FindFirstChildWhichIsA("BasePart", true)
                        if p then cPos = p.Position end
                    elseif sp:IsA("BasePart") then cPos = sp.Position 
                    else
                        local p = sp:FindFirstChildWhichIsA("BasePart", true)
                        if p then cPos = p.Position end
                    end
                end
                
                if cPos and hrp then
                    if (hrp.Position - cPos).Magnitude > 50 then
                        UpdateStatus("AUTO PITY: Moving to " .. targetBossName .. " spawner...", C.Cyan)
                        SmartTeleport(CFrame.new(cPos + Vector3.new(0, 15, 0)))
                        task.wait(1)
                        continue
                    else
                        hrp.Velocity = Vector3.zero
                    end
                end

                local isSummonable = {
                    ["AtomicBoss"]=true, ["TrueAizenBoss"]=true, ["AnosBoss"]=true, 
                    ["RimuruBoss"]=true, ["StrongestofTodayBoss"]=true, ["StrongestinHistoryBoss"]=true, 
                    ["SaberBoss"]=true, ["QinShiBoss"]=true, ["IchigoBoss"]=true, 
                    ["GilgameshBoss"]=true, ["BlessedMaidenBoss"]=true, ["SaberAlterBoss"]=true
                }

                if isSummonable[targetBossName] then
                    if not char:GetAttribute("LastPitySummon") or tick() - char:GetAttribute("LastPitySummon") > 8 then
                        char:SetAttribute("LastPitySummon", tick())
                        UpdateStatus("AUTO PITY: Summoning " .. targetBossName .. "...", C.Green)
                        
                        local re = ReplicatedStorage:FindFirstChild("RemoteEvents")
                        local rem = ReplicatedStorage:FindFirstChild("Remotes")
                        local diff = S.pitySummonDiff or "Normal"
                        
                        pcall(function()
                            if targetBossName == "AtomicBoss" and re and re:FindFirstChild("RequestSpawnAtomic") then
                                re.RequestSpawnAtomic:FireServer(diff)
                            elseif targetBossName == "TrueAizenBoss" and re and re:FindFirstChild("RequestSpawnTrueAizen") then
                                re.RequestSpawnTrueAizen:FireServer(diff)
                            elseif targetBossName == "AnosBoss" and rem and rem:FindFirstChild("RequestSpawnAnosBoss") then
                                rem.RequestSpawnAnosBoss:FireServer("Anos", diff)
                            elseif targetBossName == "RimuruBoss" and re and re:FindFirstChild("RequestSpawnRimuru") then
                                re.RequestSpawnRimuru:FireServer(diff)
                            elseif targetBossName == "StrongestofTodayBoss" and rem and rem:FindFirstChild("RequestSpawnStrongestBoss") then
                                rem.RequestSpawnStrongestBoss:FireServer("StrongestToday", diff)
                            elseif targetBossName == "StrongestinHistoryBoss" and rem and rem:FindFirstChild("RequestSpawnStrongestBoss") then
                                rem.RequestSpawnStrongestBoss:FireServer("StrongestHistory", diff)
                            elseif targetBossName == "SaberBoss" and rem and rem:FindFirstChild("RequestSummonBoss") then
                                rem.RequestSummonBoss:FireServer("SaberBoss")
                            elseif targetBossName == "QinShiBoss" and rem and rem:FindFirstChild("RequestSummonBoss") then
                                rem.RequestSummonBoss:FireServer("QinShiBoss")
                            elseif targetBossName == "IchigoBoss" and rem and rem:FindFirstChild("RequestSummonBoss") then
                                rem.RequestSummonBoss:FireServer("IchigoBoss")
                            elseif targetBossName == "GilgameshBoss" and rem and rem:FindFirstChild("RequestSummonBoss") then
                                rem.RequestSummonBoss:FireServer("GilgameshBoss", diff)
                            elseif targetBossName == "BlessedMaidenBoss" and rem and rem:FindFirstChild("RequestSummonBoss") then
                                rem.RequestSummonBoss:FireServer("BlessedMaidenBoss", diff)
                            elseif targetBossName == "SaberAlterBoss" and rem and rem:FindFirstChild("RequestSummonBoss") then
                                rem.RequestSummonBoss:FireServer("SaberAlterBoss", diff)
                            end
                        end)
                        
                        if pityCount < 24 and not S.autoHopPity then
                            local cIdx = char:GetAttribute("PityBuilderIdx") or 1
                            char:SetAttribute("PityBuilderIdx", cIdx + 1)
                        end
                    end
                else
                    UpdateStatus("AUTO PITY: Waiting for " .. targetBossName .. " to spawn...", C.Cyan)
                end
                targetMob = ""
            end
        elseif S.AutoLevelActive then
            local myLevel = getCurrentLevel()
            local bestTier = PROGRESSION_MAP[1]
            
            for _, tier in ipairs(PROGRESSION_MAP) do
                if myLevel >= tier.Level then
                    bestTier = tier
                else
                    break
                end
            end
            
            -- Instantly teleport to new island via game remote
            if S.lastPortal ~= bestTier.Portal and bestTier.Portal then
                if TeleportToPortal then
                    UpdateStatus("TELEPORTING to " .. bestTier.Portal .. "...", C.Cyan)
                    executePortalTeleport(bestTier.Portal)
                    S.lastPortal = bestTier.Portal
                    task.wait(3) -- Wait for teleport to process & chunks to load
                    continue -- Restart loop with new character position
                end
            end
            
            if S.lastAutoQuest ~= bestTier.Quest then
                if S.lastAutoQuest ~= "" then
                    if QuestAbandon then
                        pcall(function() QuestAbandon:FireServer("repeatable") end)
                        pcall(function() QuestAbandon:FireServer("Abandon") end)
                        pcall(function() QuestAbandon:FireServer(S.lastAutoQuest) end)
                        pcall(function() QuestAbandon:FireServer() end)
                    end
                    local qUI = LocalPlayer:FindFirstChild("PlayerGui") and LocalPlayer.PlayerGui:FindFirstChild("QuestUI")
                    if qUI and qUI:FindFirstChild("Quest") then qUI.Quest.Visible = false end
                    task.wait(1)
                end
                S.lastAutoQuest = bestTier.Quest
                S.forceQuestAccept = true
            end
            
            isFarming = true
            targetMob = bestTier.Mob
            manualTargets = nil
            targetQuest = bestTier.Quest
            S.AutoQuestActive = true
        elseif S.auto25Bosses then
            local keys = InventoryCache["Boss Key"] or 0
            if not HasReceivedInventory then keys = 1 end
            if keys > 0 then
                isFarming = true
                targetMob = "SaberBoss"
                manualTargets = nil
                targetQuest = ""
                
                local bossFound = false
                if NPCs then
                    for _, npc in ipairs(NPCs:GetChildren()) do
                        if string.find(string.lower(npc.Name), "saber") then
                            bossFound = true
                            targetMob = npc.Name
                            break
                        end
                    end
                end
                
                if not bossFound then
                    if S.lastPortal ~= "Boss" then
                        UpdateStatus("AUTO 25 BOSSES: Teleporting to Boss...", C.Cyan)
                        executePortalTeleport("Boss")
                        S.lastPortal = "Boss"
                        task.wait(3)
                        continue
                    end
                    if RequestSummonBoss then
                        if not char:GetAttribute("LastBossSummon") or tick() - char:GetAttribute("LastBossSummon") > 8 then
                            char:SetAttribute("LastBossSummon", tick())
                            UpdateStatus("AUTO 25 BOSSES: Summoning SaberBoss...", C.Green)
                            pcall(function() RequestSummonBoss:FireServer("SaberBoss") end)
                        end
                        targetMob = ""
                    end
                end
            else
                isFarming = true
                targetMob = "YujiBoss"
                manualTargets = nil
                targetQuest = ""
                
                local yujiFound = false
                if NPCs then
                    for _, npc in ipairs(NPCs:GetChildren()) do
                        if string.find(string.lower(npc.Name), "yuji") then
                            yujiFound = true
                            targetMob = npc.Name
                            break
                        end
                    end
                end
                
                if not yujiFound and S.lastPortal ~= "Shibuya" then
                    UpdateStatus("AUTO 25 BOSSES: Teleporting to Shibuya for keys...", C.Cyan)
                    executePortalTeleport("Shibuya")
                    S.lastPortal = "Shibuya"
                    task.wait(3)
                    continue
                end
            end
        end
        
        -- 0. Determine Island Focus Position for Spawn
        local focusPos = nil
        if isFarming and targetQuest ~= "" then
            local questNPC = Workspace:FindFirstChild(targetQuest) or (Workspace:FindFirstChild("ServiceNPCs") and Workspace.ServiceNPCs:FindFirstChild(targetQuest))
            if not questNPC then
                for _, v in ipairs(Workspace:GetChildren()) do
                    if v:IsA("Model") and v.Name == targetQuest then questNPC = v; break end
                end
            end
            if questNPC and questNPC:IsA("Model") then focusPos = questNPC:GetPivot().Position end
        end
        if not focusPos and isFarming and NPCs then
            for _, npc in ipairs(NPCs:GetChildren()) do
                if isMobTarget(npc, targetMob, manualTargets) then
                    focusPos = npc:GetPivot().Position
                    break
                end
            end
        end
        
        -- 1. Auto Set Spawn Check (Wait until we actually travel to the island)
        local isNearIsland = focusPos and hrp and (hrp.Position - focusPos).Magnitude < 1500
        if S.AutoSetSpawnActive and isFarming and hrp and focusPos and isNearIsland then
            if not char:GetAttribute("LastSpawnSearch") or tick() - char:GetAttribute("LastSpawnSearch") > 3 then
                char:SetAttribute("LastSpawnSearch", tick())
                local closestPrompt, minDist = nil, math.huge
                for _, v in ipairs(Workspace:GetDescendants()) do
                    if v:IsA("ProximityPrompt") and v.Name == "CheckpointPrompt" then
                        if v.Parent and v.Parent:IsA("BasePart") then
                            local d = (focusPos - v.Parent.Position).Magnitude
                            if d < minDist and d < 3000 then minDist = d; closestPrompt = v end
                        end
                    end
                end
                
                if closestPrompt and closestPrompt.Parent then
                    local posKey = math.floor(closestPrompt.Parent.Position.X) .. "_" .. math.floor(closestPrompt.Parent.Position.Z)
                    if not S.claimedSpawns[posKey] then
                        currentTargetCrystal = closestPrompt
                    else
                        currentTargetCrystal = nil
                    end
                else
                    currentTargetCrystal = nil
                end
            end
            
            local targetCrystal = currentTargetCrystal
            if targetCrystal and targetCrystal.Parent then
                local part = targetCrystal.Parent
                if (hrp.Position - part.Position).Magnitude > 15 then
                    UpdateStatus("AUTO SPAWN: Moving to Checkpoint...", C.Cyan)
                    SmartTeleport(CFrame.new(part.Position + Vector3.new(0, 3, 4)))
                else
                    UpdateStatus("AUTO SPAWN: Claiming Checkpoint...", C.Green)
                    hrp.CFrame = CFrame.lookAt(part.Position + Vector3.new(0, 3, 4), part.Position)
                    hrp.Velocity = Vector3.zero
                    local posKey = math.floor(part.Position.X) .. "_" .. math.floor(part.Position.Z)
                    S.lastTargetedCrystalPos = posKey
                    task.wait(0.2)
                    
                    pcall(function()
                        targetCrystal.RequiresLineOfSight = false
                        targetCrystal.MaxActivationDistance = 50
                        local cam = workspace.CurrentCamera
                        if cam then cam.CFrame = CFrame.lookAt(cam.CFrame.Position, part.Position) end
                        targetCrystal.Style = Enum.ProximityPromptStyle.Custom
                        task.wait(0.05)
                        if fireproximityprompt then 
                            fireproximityprompt(targetCrystal, 1, true)
                        else 
                            targetCrystal:InputHoldBegin(); task.wait(targetCrystal.HoldDuration + 0.1); targetCrystal:InputHoldEnd() 
                        end
                        task.delay(1.5, function()
                            if targetCrystal and targetCrystal.Parent then
                                targetCrystal.Style = Enum.ProximityPromptStyle.Default
                            end
                        end)
                    end)
                    task.wait(0.5)
                    S.claimedSpawns[posKey] = true
                    currentTargetCrystal = nil
                end
                continue -- Prioritize spawn over fighting/questing
            elseif targetCrystal and not targetCrystal.Parent then
                currentTargetCrystal = nil -- reset if unloaded
            end
        end
        
        -- 2. Sync Quest Check
        local hasQuest = false
        local questUI = LocalPlayer:FindFirstChild("PlayerGui") and LocalPlayer.PlayerGui:FindFirstChild("QuestUI")
        if questUI and questUI:FindFirstChild("Quest") and questUI.Quest.Visible then
            hasQuest = true
        end
        
        if S.forceQuestAccept then
            hasQuest = false
        end
        
        local skipEnemy = false
        if isFarming and S.AutoQuestActive and targetQuest ~= "" and not hasQuest and hrp then
            UpdateStatus("AUTO QUEST: Claiming " .. targetQuest .. " remotely...", C.Cyan)
            if QuestAccept then pcall(function() QuestAccept:FireServer(targetQuest) end) end
            task.wait(0.5)
            S.forceQuestAccept = false
        end
        
        -- 2. Enemy Farm Check
        local hasTargetConfig = (targetMob ~= "") or (manualTargets and next(manualTargets))
        if isFarming and hasTargetConfig and NPCs and hrp and not skipEnemy then
            local target = nil
            local activeMobFilter = targetMob
            local isMulti = false
            local targetListCount = 0
            
            if targetMob == "" and manualTargets and next(manualTargets) then
                isMulti = true
                local targetList = {}
                for m, v in pairs(manualTargets) do 
                    if v then 
                        table.insert(targetList, m) 
                        targetListCount = targetListCount + 1
                    end 
                end
                table.sort(targetList)
                if targetListCount > 0 then
                    local curIdx = S.currentTargetIndex or 1
                    if curIdx > #targetList then curIdx = 1; S.currentTargetIndex = curIdx end
                    activeMobFilter = targetList[curIdx]
                else
                    activeMobFilter = ""
                end
            end

            if S.lastTargetName ~= activeMobFilter then
                local oldPortal = getPortalForMob(S.lastTargetName)
                local newPortal = getPortalForMob(activeMobFilter)
                
                if oldPortal ~= newPortal then
                    S.lastPortal = "" -- Only reset if the new target is on a completely different island
                end
                S.lastTargetName = activeMobFilter
            end
            
            if currentFarmTarget and currentFarmTarget.Parent and currentFarmTarget:FindFirstChild("Humanoid") and currentFarmTarget.Humanoid.Health > 0 and currentFarmTarget:FindFirstChild("HumanoidRootPart") then
                -- Failsafe: If the enemy respawns and teleports far away, break the lock!
                if (hrp.Position - currentFarmTarget.HumanoidRootPart.Position).Magnitude > 1500 then
                    currentFarmTarget = nil
                else
                    target = currentFarmTarget
                end
            end
            
            if not target and currentFarmTarget then
                currentFarmTarget = nil
            end
            
            local portalToUse = getPortalForMob(activeMobFilter)
            
            if portalToUse and S.lastPortal ~= portalToUse then
                UpdateStatus("AUTO FARM: Teleporting to " .. portalToUse .. "...", C.Cyan)
                executePortalTeleport(portalToUse)
                S.lastPortal = portalToUse
                task.wait(3.5)
                continue
            end
            
            if not target then
                currentFarmTarget = nil
                local closest = nil
                local minHp = math.huge
                local minDist = math.huge
                local unloadedMobPos = nil
                
                for _, npc in ipairs(NPCs:GetChildren()) do
                    if isMobTarget(npc, activeMobFilter, nil) then
                        if npc:IsA("Model") then
                            local hum = npc:FindFirstChild("Humanoid")
                            local root = npc:FindFirstChild("HumanoidRootPart")
                            if hum and hum.Health > 0 and root then
                                if hum.Health < minHp then
                                    minHp = hum.Health
                                    minDist = (hrp.Position - root.Position).Magnitude
                                    closest = npc
                                elseif hum.Health == minHp then
                                    local d = (hrp.Position - root.Position).Magnitude
                                    if d < minDist then
                                        minDist = d
                                        closest = npc
                                    end
                                end
                            elseif not root then
                                unloadedMobPos = npc:GetPivot().Position
                            end
                        end
                    end
                end
                target = closest
                
                if not target and unloadedMobPos then
                    UpdateStatus("AUTO FARM: Streaming chunk for unloaded targets...", C.Cyan)
                    pcall(function() LocalPlayer:RequestStreamAroundAsync(unloadedMobPos) end)
                    SmartTeleport(CFrame.new(unloadedMobPos + Vector3.new(0, 10, 0)))
                end
                
                if not target and not unloadedMobPos and activeMobFilter and string.find(string.lower(activeMobFilter), "boss") then
                    local currentBossKey = activeMobFilter
                    for _, bName in ipairs(BOSS_LIST) do
                        local cleanBName = string.gsub(string.lower(bName), "boss", "")
                        local noSpaceMob = string.gsub(activeMobFilter, "%s+", "")
                        if string.find(string.lower(noSpaceMob), string.lower(bName)) or string.find(string.lower(noSpaceMob), string.lower(cleanBName)) then
                            currentBossKey = bName
                            break
                        end
                    end
                    
                    local cont
                    local names = {
                        "TimedBossSpawn_" .. currentBossKey .. "_Container",
                        "TimedBossSpawn_" .. currentBossKey,
                    }
                    local cleanBName = string.gsub(currentBossKey, "Boss", "")
                    table.insert(names, "TimedBossSpawn_" .. cleanBName .. "_Container")
                    table.insert(names, "TimedBossSpawn_" .. cleanBName)
                    
                    if string.lower(currentBossKey) == "yamatoboss" then
                        table.insert(names, "TimedBossSpawn_Yamato_Container")
                        table.insert(names, "TimedBossSpawn_Yamato")
                    end
                    for _, name in ipairs(names) do
                        cont = Workspace:FindFirstChild(name)
                        if cont then break end
                    end
                    
                    if cont then
                        local sp = cont:FindFirstChild("TimedBossSpawn_" .. currentBossKey) or cont
                        local cPos = nil
                        if sp:IsA("Model") and sp.PrimaryPart then cPos = sp:GetPivot().Position
                        elseif sp:IsA("Model") then 
                            local p = sp:FindFirstChildWhichIsA("BasePart", true)
                            if p then cPos = p.Position end
                        elseif sp:IsA("BasePart") then cPos = sp.Position 
                        else
                            local p = sp:FindFirstChildWhichIsA("BasePart", true)
                            if p then cPos = p.Position end
                        end
                        
                        if cPos then
                            local attempts = char:GetAttribute("BossLoadAttempts") or 0
                            if attempts < 4 then
                                UpdateStatus("AUTO FARM: Flying to Spawner to load Boss...", C.Cyan)
                                char:SetAttribute("BossLoadAttempts", attempts + 1)
                                if hrp and (hrp.Position - cPos).Magnitude > 50 then
                                    pcall(function() LocalPlayer:RequestStreamAroundAsync(cPos) end)
                                    SmartTeleport(CFrame.new(cPos + Vector3.new(0, 15, 0)))
                                else
                                    if hrp then hrp.Velocity = Vector3.zero end
                                end
                                task.wait(3)
                            else
                                UpdateStatus("AUTO FARM: Boss appears dead or unloaded, skipping...", C.Gray)
                                char:SetAttribute("BossLoadAttempts", 0)
                                LocallyDeadBosses[currentBossKey] = tick()
                                if BossTimersCache[currentBossKey] then
                                    BossTimersCache[currentBossKey].isAlive = false
                                    BossTimersCache[currentBossKey].state = "DEAD"
                                    BossTimersCache[currentBossKey].timer = 3600
                                end
                                task.wait(2)
                            end
                        end
                    end
                end
            end
            
            if target then
                local thrp = target:FindFirstChild("HumanoidRootPart")
                if currentFarmTarget ~= target then
                    task.wait(0.5)
                    if not target or not target.Parent or not target:FindFirstChild("Humanoid") or target.Humanoid.Health <= 0 then
                        currentFarmTarget = nil
                        continue
                    end
                char:SetAttribute("BossLoadAttempts", 0)
                end
                char:SetAttribute("WaitStartTime", nil)
                currentFarmTarget = target
                local thrp = target:FindFirstChild("HumanoidRootPart")
                
                -- Mega Hitbox & Stun (Untouchable Above Farm)
                pcall(function()
                    if target:FindFirstChild("Humanoid") then
                        target.Humanoid.WalkSpeed = 0
                        target.Humanoid.JumpPower = 0
                    end
                    if thrp then
                        thrp.CanCollide = false
                        thrp.Velocity = Vector3.zero
                    end
                end)
                
                if thrp then
                    local isBoss = false
                    if activeMobFilter and activeMobFilter ~= "" then
                        isBoss = string.find(string.lower(activeMobFilter), "boss") ~= nil
                    elseif target then
                        isBoss = string.find(string.lower(target.Name), "boss") ~= nil
                    end
                    
                    local validNearbyCount = 0
                    for _, npc in ipairs(NPCs:GetChildren()) do
                        if isMobTarget(npc, activeMobFilter, nil) then
                            local nHum = npc:FindFirstChild("Humanoid")
                            local nRoot = npc:FindFirstChild("HumanoidRootPart")
                            if nHum and nHum.Health > 0 and nRoot and (hrp.Position - nRoot.Position).Magnitude < 1000 then
                                validNearbyCount = validNearbyCount + 1
                            end
                        end
                    end
                    
                    local wantsAura = (S.autoKillAuraQuest or S.mainFarmMode == "Kill Aura") and not isBoss
                    local useAura = wantsAura and (validNearbyCount >= 2)
                    
                    if useAura then
                        UpdateStatus("AUTO FARM: Kill Aura mode engaging " .. target.Name, C.Green)
                        
                        local goalCF = getFarmPosition(thrp, S.farmDistance, S.farmPos)
                        
                        if not char:GetAttribute("IsStriking") then
                            if (hrp.Position - goalCF.Position).Magnitude > 50 then
                                local fbv = hrp:FindFirstChild("JomFarmBV")
                                if fbv then fbv:Destroy() end
                                SmartTeleport(goalCF)
                            else
                                local fbv = hrp:FindFirstChild("JomFarmBV")
                                if not fbv then
                                    fbv = Instance.new("BodyVelocity")
                                    fbv.Name = "JomFarmBV"; fbv.MaxForce = Vector3.new(math.huge, math.huge, math.huge); fbv.Velocity = Vector3.zero; fbv.Parent = hrp
                                end
                                for _, v in ipairs(hrp:GetChildren()) do
                                    if (v:IsA("BodyVelocity") and v.Name ~= "JomFarmBV" and v.Name ~= "JomTeleportBV") or v:IsA("BodyPosition") or v:IsA("LinearVelocity") or v:IsA("AlignPosition") then
                                        v:Destroy()
                                    end
                                end
                                -- Stop spamming CFrame updates if we are already at the target
                                if (hrp.Position - goalCF.Position).Magnitude > 5 then
                                    hrp.CFrame = goalCF
                                end
                            end
                        end
                        
                        local hum = char:FindFirstChild("Humanoid")
                        if hum then
                            if not char:GetAttribute("IsStriking") and (not char:GetAttribute("LastAttack") or tick() - char:GetAttribute("LastAttack") > (0.4 / S.fastAttack)) then
                                char:SetAttribute("LastAttack", tick())
                                char:SetAttribute("IsStriking", true)
                                
                                task.spawn(function()
                                    local originalPos = hrp.CFrame
                                    
                                    local enemiesToHit = {}
                                    for _, npc in ipairs(NPCs:GetChildren()) do
                                        if isMobTarget(npc, activeMobFilter, nil) then
                                            local nHum = npc:FindFirstChild("Humanoid")
                                            local nRoot = npc:FindFirstChild("HumanoidRootPart")
                                            if nHum and nHum.Health > 0 and nRoot and nRoot:IsDescendantOf(workspace) then
                                                if (originalPos.Position - nRoot.Position).Magnitude < 400 then
                                                    table.insert(enemiesToHit, nRoot)
                                                    pcall(function()
                                                        nHum.WalkSpeed = 0
                                                        nHum.JumpPower = 0
                                                        nRoot.CanCollide = false
                                                        nRoot.Velocity = Vector3.zero
                                                    end)
                                                    if #enemiesToHit >= 15 then break end
                                                end
                                            end
                                        end
                                    end
                                    
                                    if #enemiesToHit > 0 then
                                        local weapons = getWeaponsToUse()
                                        if #weapons == 0 then weapons = {nil} end
                                        
                                        for _, wep in ipairs(weapons) do
                                            if wep and wep.Parent ~= char then
                                                pcall(function() hum:EquipTool(wep) end)
                                                task.wait(0.05)
                                            end
                                            for _, nRoot in ipairs(enemiesToHit) do
                                                if nRoot and nRoot.Parent then
                                                    hrp.CFrame = getFarmPosition(nRoot, S.farmDistance, S.farmPos)
                                                    hrp.Velocity = Vector3.zero
                                                    task.wait(0.02)
                                                    
                                                    for _ = 1, 2 do
                                                        if RequestHit then pcall(function() RequestHit:FireServer(nRoot.Parent) end) end
                                                        if CombatRemote then pcall(function() CombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                                        if KatanaCombatRemote then pcall(function() KatanaCombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                                    end
                                                    
                                                    if RequestAbility then
                                                        if S.autoSkills["Skill Z"] then pcall(function() RequestAbility:FireServer(1) end) end
                                                        if S.autoSkills["Skill X"] then pcall(function() RequestAbility:FireServer(2) end) end
                                                        if S.autoSkills["Skill C"] then pcall(function() RequestAbility:FireServer(3) end) end
                                                        if S.autoSkills["Skill V"] then pcall(function() RequestAbility:FireServer(4) end) end
                                                        if S.autoSkills["Skill F"] then pcall(function() RequestAbility:FireServer(5) end) end
                                                    end
                                                end
                                            end
                                        end
                                        
                                        hrp.CFrame = originalPos
                                        hrp.Velocity = Vector3.zero
                                    end
                                    
                                    char:SetAttribute("IsStriking", false)
                                end)
                            end
                        end
                    else
                        if wantsAura then
                            UpdateStatus("AUTO FARM: Aura Paused (<2 Enemies) - Engaging " .. target.Name, C.Green)
                        else
                            UpdateStatus("AUTO FARM: Engaging " .. target.Name, C.Green)
                        end
                        
                        local goalCF = getFarmPosition(thrp, S.farmDistance, S.farmPos)
                        
                        if not char:GetAttribute("IsStriking") then
                            if (hrp.Position - goalCF.Position).Magnitude > 50 then
                                local fbv = hrp:FindFirstChild("JomFarmBV")
                                if fbv then fbv:Destroy() end
                                SmartTeleport(goalCF)
                            else
                                local fbv = hrp:FindFirstChild("JomFarmBV")
                                if not fbv then
                                    fbv = Instance.new("BodyVelocity")
                                    fbv.Name = "JomFarmBV"; fbv.MaxForce = Vector3.new(math.huge, math.huge, math.huge); fbv.Velocity = Vector3.zero; fbv.Parent = hrp
                                end
                                for _, v in ipairs(hrp:GetChildren()) do
                                    if (v:IsA("BodyVelocity") and v.Name ~= "JomFarmBV" and v.Name ~= "JomTeleportBV") or v:IsA("BodyPosition") or v:IsA("LinearVelocity") or v:IsA("AlignPosition") then
                                        v:Destroy()
                                    end
                                end
                                hrp.CFrame = goalCF
                            end
                        end
                        
                        local hum = char:FindFirstChild("Humanoid")
                        if hum then
                            if not char:GetAttribute("LastAttack") or tick() - char:GetAttribute("LastAttack") > math.max(0.1, 0.5 / S.fastAttack) then
                                char:SetAttribute("LastAttack", tick())
                                
                                local weapons = getWeaponsToUse()
                                if #weapons == 0 then weapons = {nil} end
                                
                                for _, wep in ipairs(weapons) do
                                    if wep and wep.Parent ~= char then
                                        pcall(function() hum:EquipTool(wep) end)
                                        task.wait(0.05)
                                    end
                                    
                                    for _ = 1, 2 do
                                        if RequestHit then pcall(function() RequestHit:FireServer(thrp.Parent) end) end
                                        if CombatRemote then pcall(function() CombatRemote:FireServer("Punch", thrp.Parent) end) end
                                        if KatanaCombatRemote then pcall(function() KatanaCombatRemote:FireServer("Punch", thrp.Parent) end) end
                                    end
                                    
                                    if RequestAbility then
                                        if S.autoSkills["Skill Z"] then pcall(function() RequestAbility:FireServer(1) end) end
                                        if S.autoSkills["Skill X"] then pcall(function() RequestAbility:FireServer(2) end) end
                                        if S.autoSkills["Skill C"] then pcall(function() RequestAbility:FireServer(3) end) end
                                        if S.autoSkills["Skill V"] then pcall(function() RequestAbility:FireServer(4) end) end
                                        if S.autoSkills["Skill F"] then pcall(function() RequestAbility:FireServer(5) end) end
                                    end
                                end
                            end
                        end
                    end
                end
            else
                currentFarmTarget = nil
                UpdateStatus("AUTO FARM: Waiting for targets to spawn...", C.Cyan)
            
                if hrp then
                    local fbv = hrp:FindFirstChild("JomFarmBV")
                    if not fbv then
                        fbv = Instance.new("BodyVelocity"); fbv.Name = "JomFarmBV"; fbv.MaxForce = Vector3.new(math.huge, math.huge, math.huge); fbv.Velocity = Vector3.zero; fbv.Parent = hrp
                    end
                end
                if not char:GetAttribute("WaitStartTime") then
                    char:SetAttribute("WaitStartTime", tick())
                elseif tick() - char:GetAttribute("WaitStartTime") > 4 then
                    char:SetAttribute("WaitStartTime", nil)
                    if isMulti and targetListCount > 1 then
                        S.currentTargetIndex = (S.currentTargetIndex or 1) + 1
                        if S.currentTargetIndex > targetListCount then S.currentTargetIndex = 1 end
                    end
                end
            end
        else
            currentFarmTarget = nil
            if hrp then
                local fbv = hrp:FindFirstChild("JomFarmBV")
                if fbv then fbv:Destroy() end
            end
        end
        
        task.wait() -- Single synchronized max-speed loop
    end
end)

-- 10. AFK AURA & ISLAND HOP LOOP
task.spawn(function()
    local lastHopTime = 0
    local currentIslandIndex = 1
    
    while _G.JomHubRunning do
        local char = LocalPlayer.Character
        local hrp = char and char:FindFirstChild("HumanoidRootPart")
        local hum = char and char:FindFirstChild("Humanoid")
        
        if (S.afkAura or S.afkIslandHop) and hrp and hum and hum.Health > 0 then
            local currentAuraDist = S.afkIslandHop and S.afkIslandHopDist or S.localAuraDist
            
            -- Island Hop Logic
            if S.afkIslandHop then
                local selectedIslands = {}
                for island, isSelected in pairs(S.afkIslands) do
                    if isSelected then table.insert(selectedIslands, island) end
                end
                table.sort(selectedIslands)
                
                if #selectedIslands > 0 then
                    local remaining = math.ceil(S.afkHopDelay - (tick() - lastHopTime))
                    if lastHopTime == 0 then remaining = 0 end
                    
                    local shouldHopNow = false
                    -- Wait 8 seconds before assuming an island is cleared to allow slow chunks to load
                    if S.afkHopWhenCleared and lastHopTime ~= 0 and (tick() - lastHopTime > 8) then
                        local enemiesAlive = false
                        if NPCs then
                            for _, npc in ipairs(NPCs:GetChildren()) do
                                local nHum = npc:FindFirstChild("Humanoid")
                                local nRoot = npc:FindFirstChild("HumanoidRootPart")
                                if nHum and nHum.Health > 0 and nRoot then
                                    if (hrp.Position - nRoot.Position).Magnitude <= currentAuraDist then
                                        local shouldCheck = true
                                        if S.afkIslandHop and not S.afkAura then
                                            local mobIsland = getPortalForMob(npc.Name)
                                            if mobIsland ~= S.lastPortal then
                                                shouldCheck = false
                                            end
                                        end
                                        if shouldCheck then
                                            enemiesAlive = true
                                            break
                                        end
                                    end
                                end
                            end
                        end
                        if not enemiesAlive then shouldHopNow = true end
                    end
                    
                    if remaining <= 0 or shouldHopNow then
                        if currentIslandIndex > #selectedIslands then currentIslandIndex = 1 end
                        local targetIsland = selectedIslands[currentIslandIndex]
                        
                        local isAlreadyOnTarget = (S.lastPortal == targetIsland)
                        local activelyAttacking = false
                        
                        if NPCs then
                            for _, npc in ipairs(NPCs:GetChildren()) do
                                local nRoot = npc:FindFirstChild("HumanoidRootPart")
                                if nRoot and (hrp.Position - nRoot.Position).Magnitude <= 4500 then
                                    local mobIsland = getPortalForMob(npc.Name)
                                    if mobIsland == targetIsland then
                                        isAlreadyOnTarget = true
                                    end
                                    if mobIsland == S.lastPortal and (hrp.Position - nRoot.Position).Magnitude <= currentAuraDist + 50 then
                                        local nHum = npc:FindFirstChild("Humanoid")
                                        if nHum and nHum.Health > 0 then activelyAttacking = true end
                                    end
                                end
                            end
                        end
                        
                        if isAlreadyOnTarget then
                            if shouldHopNow and #selectedIslands > 1 then
                                currentIslandIndex = currentIslandIndex + 1
                                if currentIslandIndex > #selectedIslands then currentIslandIndex = 1 end
                                targetIsland = selectedIslands[currentIslandIndex]
                            else
                                S.lastPortal = targetIsland
                                lastHopTime = tick()
                                currentIslandIndex = currentIslandIndex + 1
                                continue
                            end
                        end
                        
                        if activelyAttacking and not shouldHopNow then
                            task.wait(1)
                            lastHopTime = tick() - S.afkHopDelay + 2
                            continue
                        end
                        
                        UpdateStatus("AFK HOP: Preparing hop to " .. targetIsland .. "...", C.Cyan)
                        if AfkHopTimerLabel then AfkHopTimerLabel.Text = "Teleporting..."; AfkHopTimerLabel.TextColor3 = C.Orange end
                        
                        task.wait(3) -- Initial pause before teleporting
                        
                        executePortalTeleport(targetIsland)
                        S.lastPortal = targetIsland
                        
                        task.wait(2) -- Let the server load enemies before attacking
                        
                        lastHopTime = tick()
                        currentIslandIndex = currentIslandIndex + 1
                        continue
                    else
                        if AfkHopTimerLabel then
                            AfkHopTimerLabel.Text = "Next Hop: " .. remaining .. "s"
                            AfkHopTimerLabel.TextColor3 = C.Cyan
                        end
                    end
                else
                    if AfkHopTimerLabel then
                        AfkHopTimerLabel.Text = "No Islands Selected"
                        AfkHopTimerLabel.TextColor3 = C.Gray
                    end
                end
            else
                if AfkHopTimerLabel then
                    AfkHopTimerLabel.Text = "Next Hop: --"
                    AfkHopTimerLabel.TextColor3 = C.Gray
                end
            end
            
            -- Aura Kill Logic
            local originalPos = hrp.CFrame
            local enemiesToHit = {}
            
            if NPCs then
                for _, npc in ipairs(NPCs:GetChildren()) do
                    local nHum = npc:FindFirstChild("Humanoid")
                    local nRoot = npc:FindFirstChild("HumanoidRootPart")
                    if nHum and nHum.Health > 0 and nRoot then
                        if (originalPos.Position - nRoot.Position).Magnitude <= currentAuraDist then
                            local shouldAttack = true
                            if S.afkIslandHop and not S.afkAura then
                                local mobIsland = getPortalForMob(npc.Name)
                                if mobIsland ~= S.lastPortal then
                                    shouldAttack = false
                                end
                            end
                            if shouldAttack then
                                table.insert(enemiesToHit, nRoot)
                                pcall(function()
                                    nHum.WalkSpeed = 0
                                    nHum.JumpPower = 0
                                    if S.islandHopMode ~= "Single Target" then
                                        nRoot.Transparency = 0.99
                                    end
                                    nRoot.CanCollide = false
                                    nRoot.Velocity = Vector3.zero
                                end)
                                local maxHit = S.islandHopMode == "Single Target" and 1 or 15
                                if #enemiesToHit >= maxHit then break end
                            end
                        end
                    end
                end
            end
            
            if #enemiesToHit > 0 then
                local cam = workspace.CurrentCamera
                local fakeCamPart = nil
                
                if S.islandHopMode ~= "Single Target" then
                    fakeCamPart = Instance.new("Part")
                    fakeCamPart.Transparency = 1
                    fakeCamPart.Anchored = true
                    fakeCamPart.CanCollide = false
                    fakeCamPart.CFrame = originalPos
                    fakeCamPart.Parent = workspace
                    cam.CameraSubject = fakeCamPart
                    
                    task.wait(2)
                end
                
                char:SetAttribute("IsStriking", true)
                
                local weapons = getWeaponsToUse()
                if #weapons == 0 then weapons = {nil} end
                
                for _, wep in ipairs(weapons) do
                    if wep and wep.Parent ~= char then
                        pcall(function() hum:EquipTool(wep) end)
                        task.wait(0.05)
                    end
                    
                    for _, nRoot in ipairs(enemiesToHit) do
                        if nRoot and nRoot.Parent then
                            hrp.CFrame = getFarmPosition(nRoot, S.farmDistance, S.farmPos)
                            hrp.Velocity = Vector3.zero
                            task.wait(0.02)
                            
                            for _ = 1, 2 do
                                if RequestHit then pcall(function() RequestHit:FireServer(nRoot.Parent) end) end
                                if CombatRemote then pcall(function() CombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                if KatanaCombatRemote then pcall(function() KatanaCombatRemote:FireServer("Punch", nRoot.Parent) end) end
                            end
                            
                            if RequestAbility then
                                if S.autoSkills["Skill Z"] then pcall(function() RequestAbility:FireServer(1) end) end
                                if S.autoSkills["Skill X"] then pcall(function() RequestAbility:FireServer(2) end) end
                                if S.autoSkills["Skill C"] then pcall(function() RequestAbility:FireServer(3) end) end
                                if S.autoSkills["Skill V"] then pcall(function() RequestAbility:FireServer(4) end) end
                                if S.autoSkills["Skill F"] then pcall(function() RequestAbility:FireServer(5) end) end
                            end
                        end
                    end
                end
                
                if S.islandHopMode ~= "Single Target" then
                    hrp.CFrame = originalPos
                    hrp.Velocity = Vector3.zero
                    task.wait(0.02)
                    cam.CameraSubject = hum
                    if fakeCamPart then fakeCamPart:Destroy() end
                end
                char:SetAttribute("IsStriking", false)
            end
        end
        task.wait(0.1)
    end
end)

-- 11. AUTO MERCHANT LOOP
task.spawn(function()
    while _G.JomHubRunning do
        if S.autoBuyMerchant then
            local MerchantRemotes = RemotesFolder and RemotesFolder:FindFirstChild("MerchantRemotes")
            local GetMerchantStock = MerchantRemotes and MerchantRemotes:FindFirstChild("GetMerchantStock")
            local PurchaseMerchantItem = MerchantRemotes and MerchantRemotes:FindFirstChild("PurchaseMerchantItem")
            
            if GetMerchantStock and PurchaseMerchantItem then
                local s, r = pcall(function() return GetMerchantStock:InvokeServer(false) end)
                if s and type(r) == "table" then
                            local function findStock(t)
                                if type(t) ~= "table" then return nil end
                                if t.stock then return t end
                                for _, v in pairs(t) do
                                    if type(v) == "table" and v.stock then return v end
                                end
                                return nil
                            end
                            local data = findStock(r)
                    if data and data.stock then
                        local hints = LocalPlayer:FindFirstChild("PlayerGui") and LocalPlayer.PlayerGui:FindFirstChild("GamepadHintsUI") and LocalPlayer.PlayerGui.GamepadHintsUI:FindFirstChild("GamepadHintsController")
                        for itemName, isSelected in pairs(S.buyMerchantItems) do
                            if isSelected and data.stock[itemName] and data.stock[itemName].stock > 0 then
                                UpdateStatus("MERCHANT: Buying " .. itemName .. "...", C.Cyan)
                                        pcall(function() PurchaseMerchantItem:InvokeServer(itemName) end)
                                        pcall(function() PurchaseMerchantItem:InvokeServer(itemName, 1) end)
                                        if hints then
                                            pcall(function() PurchaseMerchantItem:InvokeServer(hints, itemName) end)
                                        end                                
                                        task.wait(1)
                            end
                        end
                    end
                end
            end
        end
        task.wait(5)
    end
end)

-- 2. AUTO QUEST LOOP
task.spawn(function()
    while _G.JomHubRunning do
        local qName = S.questName
        local qActive = S.AutoQuestActive
        
        if S.AutoLevelActive then
            local myLevel = LocalPlayer:FindFirstChild("leaderstats") and LocalPlayer.leaderstats:FindFirstChild("Level") and LocalPlayer.leaderstats.Level.Value or 1
            if LocalPlayer:GetAttribute("Level") then myLevel = LocalPlayer:GetAttribute("Level") end
            
            local bestTier = PROGRESSION_MAP[1]
            for _, tier in ipairs(PROGRESSION_MAP) do
                if myLevel >= tier.Level then bestTier = tier else break end
            end
            qName = bestTier.Quest
            qActive = true
        end
        
        if qActive and qName ~= "" then
            -- Usually games auto-complete or require you to hit QuestComplete
            if QuestComplete then
                pcall(function() QuestComplete:FireServer(qName) end)
            end
        end
        task.wait(3)
    end
end)

-- 4. ESP LOOP
local ESP_Cache = {}
task.spawn(function()
    while _G.JomHubRunning do
        if S.EspEnabled then
            local currentFrame = {}
            
            if S.EspPlayers then
                for _, p in ipairs(Players:GetPlayers()) do
                    if p ~= LocalPlayer and p.Character and p.Character:FindFirstChild("HumanoidRootPart") then
                        currentFrame[p.Character] = string.format('<font color="rgb(255,255,255)">[P] %s</font>', p.Name)
                    end
                end
            end
            
            if S.EspEnemies and NPCs then
                for _, npc in ipairs(NPCs:GetChildren()) do
                    if npc:IsA("Model") and npc:FindFirstChild("HumanoidRootPart") then
                        local hum = npc:FindFirstChild("Humanoid")
                        if hum and hum.Health > 0 then
                            currentFrame[npc] = string.format('<font color="rgb(255,50,50)">[E] %s [%d/%d]</font>', npc.Name, math.floor(hum.Health), math.floor(hum.MaxHealth))
                        end
                    end
                end
            end
            
            if S.EspFruits then
                for _, f in ipairs(Workspace:GetChildren()) do
                    if f:IsA("Tool") or (f:IsA("Model") and string.find(string.lower(f.Name), "fruit")) then
                        currentFrame[f] = string.format('<font color="rgb(255,215,0)">[F] %s</font>', f.Name)
                    end
                end
            end
            
            if S.EspBossTimers then
                for _, info in pairs(BossTimersCache) do
                    if info.spawnPointName then
                        local sp = Workspace:FindFirstChild(info.spawnPointName, true)
                        if sp and sp:IsA("BasePart") then
                            local text = ""
                            if info.isAlive then
                                text = string.format('<font color="rgb(255,50,50)">[BOSS] %s is ALIVE!</font>', info.displayName or "Unknown")
                            else
                                local t = math.max(0, info.timer or 0); local m = math.floor(t / 60); local s = t % 60
                                text = string.format('<font color="rgb(255,170,0)">[BOSS] %s: %02d:%02d</font>', info.displayName or "Unknown", m, s)
                            end
                            currentFrame[sp] = text
                        end
                    end
                end
            end
            
            for obj, text in pairs(currentFrame) do
                local bb = ESP_Cache[obj]
                if bb and bb.Parent ~= obj then pcall(function() bb.Parent = obj end) end
                if not bb then
                    bb = Instance.new("BillboardGui"); bb.Name = "JomESP"; bb.Adornee = obj; bb.Size = UDim2.new(0, 120, 0, 40); bb.StudsOffset = Vector3.new(0, 2, 0); bb.AlwaysOnTop = true; bb.Parent = obj
                    local txt = Instance.new("TextLabel"); txt.Name = "ESPText"; txt.Size = UDim2.new(1, 0, 1, 0); txt.BackgroundTransparency = 1; txt.Text = text; txt.RichText = true; txt.TextStrokeTransparency = 0; txt.Font = Enum.Font.GothamBold; txt.TextSize = 12; txt.Parent = bb
                    ESP_Cache[obj] = bb
                else
                    local txt = bb:FindFirstChild("ESPText")
                    if txt then txt.Text = text end
                end
            end
            for obj, bb in pairs(ESP_Cache) do
                if not currentFrame[obj] or not obj.Parent then bb:Destroy(); ESP_Cache[obj] = nil end
            end
        else
            for obj, bb in pairs(ESP_Cache) do bb:Destroy(); ESP_Cache[obj] = nil end
        end
        task.wait(0.5)
    end
end)

task.spawn(function()
    while _G.JomHubRunning do
        for k, info in pairs(BossTimersCache) do
            if not info.isAlive and info.timer and info.timer > 0 then
                info.timer = info.timer - 1
            end
        end
        task.wait(1)
    end
end)

-- 7. AUTO REROLL LOOP
task.spawn(function()
    while _G.JomHubRunning do
        local currentRace = "Unknown"
        local currentClan = "Unknown"
        
        if GetPlayerStats then
            local s, r = pcall(function()
                local hints = LocalPlayer:FindFirstChild("PlayerGui") and LocalPlayer.PlayerGui:FindFirstChild("GamepadHintsUI") and LocalPlayer.PlayerGui.GamepadHintsUI:FindFirstChild("GamepadHintsController")
                return GetPlayerStats:InvokeServer(hints)
            end)
            if s and type(r) == "table" then
                local data = r[1] or r
                if data and data.Inventory and data.Inventory.Equipped then
                    if data.Inventory.Equipped.Race then currentRace = data.Inventory.Equipped.Race end
                    if data.Inventory.Equipped.Clan then currentClan = data.Inventory.Equipped.Clan end
                end
            end
        end
        
        if currentRace == "Unknown" and LocalPlayer:GetAttribute("Race") then currentRace = LocalPlayer:GetAttribute("Race") end
        if currentClan == "Unknown" and LocalPlayer:GetAttribute("Clan") then currentClan = LocalPlayer:GetAttribute("Clan") end
        
        if RaceLabel then
            RaceLabel.Text = "Current Race: " .. tostring(currentRace)
        end
        if ClanLabel then
            ClanLabel.Text = "Current Clan: " .. tostring(currentClan)
        end
        
        if S.autoRaceReroll and UseItem then
            local matched = false
            for target, isSelected in pairs(S.targetRaces) do
                if isSelected then
                    local cleanTarget = string.gsub(string.lower(target), "%s+", "")
                    local cleanCurrent = string.gsub(string.lower(tostring(currentRace)), "%s+", "")
                    
                    if cleanTarget == cleanCurrent then
                        matched = true
                        break
                    end
                end
            end
            
            if matched then
                S.autoRaceReroll = false
                UpdateStatus("RACE REROLL: Target Race Reached! (" .. tostring(currentRace) .. ")", C.Green)
            else
                local hasRerollItem = true
                if next(InventoryCache) ~= nil then 
                    if not InventoryCache["Race Reroll"] or InventoryCache["Race Reroll"] <= 0 then hasRerollItem = false end
                end
                
                if hasRerollItem then
                    UpdateStatus("RACE REROLL: Rolling... Current: " .. tostring(currentRace), C.Cyan)
                    pcall(function() UseItem:FireServer("Use", "Race Reroll", 1, true) end)
                    task.wait(1.5)
                else
                    UpdateStatus("RACE REROLL: No Race Rerolls left!", C.Gray)
                    S.autoRaceReroll = false
                end
            end
        elseif S.autoClanReroll and UseItem then
            local matched = false
            for target, isSelected in pairs(S.targetClans) do
                if isSelected then
                    local cleanTarget = string.gsub(string.lower(target), "%s+", "")
                    local cleanCurrent = string.gsub(string.lower(tostring(currentClan)), "%s+", "")
                    
                    if cleanTarget == cleanCurrent then
                        matched = true
                        break
                    end
                end
            end
            
            if matched then
                S.autoClanReroll = false
                UpdateStatus("CLAN REROLL: Target Clan Reached! (" .. tostring(currentClan) .. ")", C.Green)
            else
                local hasRerollItem = true
                if next(InventoryCache) ~= nil then 
                    if not InventoryCache["Clan Reroll"] or InventoryCache["Clan Reroll"] <= 0 then hasRerollItem = false end
                end
                
                if hasRerollItem then
                    UpdateStatus("CLAN REROLL: Rolling... Current: " .. tostring(currentClan), C.Cyan)
                    pcall(function() UseItem:FireServer("Use", "Clan Reroll", 1, true) end)
                    task.wait(1.5)
                else
                    UpdateStatus("CLAN REROLL: No Clan Rerolls left!", C.Gray)
                    S.autoClanReroll = false
                end
            end
        end
        task.wait(0.5)
    end
end)

-- 3. AUTO STATS LOOP
task.spawn(function()
    while _G.JomHubRunning do
        if S.AutoStatsActive and AllocateStat then
            local statMapping = {
                Melee = "Melee",
                Defense = "Defense",
                Sword = "Sword",
                DevilFruit = "Power"
            }
            
            for key, isEnabled in pairs(S.stats) do
                if isEnabled then
                    pcall(function() AllocateStat:FireServer(statMapping[key], S.statAmount) end)
                end
            end
        end
        task.wait(1)
    end
end)

-- 7. AUTO DUNGEON DISCOVERY LOOP
task.spawn(function()
    local DUNGEON_ISLANDS = {
        { Folder = "StarterIsland", Portal = "Starter" },
        { Folder = "JungleIsland", Portal = "Jungle" },
        { Folder = "DesertIsland", Portal = "Desert" },
        { Folder = "SnowIsland", Portal = "Snow" },
        { Folder = "ShibuyaStation", Portal = "Shibuya" },
        { Folder = "HuecoMundo", Portal = "HollowIsland" }
    }
    
    while _G.JomHubRunning do
        if S.autoDungeonDiscovery then
            local char = LocalPlayer.Character
            local hrp = char and char:FindFirstChild("HumanoidRootPart")
            if hrp then
                if QuestAccept and not S.dungeonQuestAccepted then
                    pcall(function() QuestAccept:FireServer("DungeonPortalsNPC") end)
                    pcall(function() QuestAccept:FireServer("DungeonUnlock") end)
                    S.dungeonQuestAccepted = true
                    task.wait(0.5)
                end
                
                local allDone = true
                for _, islandInfo in ipairs(DUNGEON_ISLANDS) do
                    if not S.autoDungeonDiscovery then break end
                    if S.checkedDungeonPieces[islandInfo.Folder] then continue end
                    
                    allDone = false
                    local islandFolder = Workspace:FindFirstChild(islandInfo.Folder)
                    local piece = islandFolder and islandFolder:FindFirstChild("DungeonPuzzlePiece")
                    
                    if not piece then
                        if S.lastPortal ~= islandInfo.Portal then
                            if TeleportToPortal then
                                UpdateStatus("DUNGEON: Teleporting to " .. islandInfo.Portal .. "...", C.Cyan)
                                executePortalTeleport(islandInfo.Portal)
                                S.lastPortal = islandInfo.Portal
                                task.wait(3) -- Wait for chunk to load safely
                                break -- Restart loop to check if piece loaded
                            end
                        else
                            -- Teleported and waited, but piece is still missing. Assume collected.
                            S.checkedDungeonPieces[islandInfo.Folder] = true
                        end
                    else
                        local prompt = piece:FindFirstChild("PuzzlePrompt")
                        if prompt and prompt.Enabled then
                            UpdateStatus("DUNGEON: Collecting piece at " .. islandInfo.Folder, C.Green)
                            local targetPart = prompt.Parent:IsA("BasePart") and prompt.Parent or piece
                            local pPos = targetPart:IsA("Model") and targetPart:GetPivot().Position or targetPart.Position
                            pcall(function() LocalPlayer:RequestStreamAroundAsync(pPos) end)
                            
                            if (hrp.Position - pPos).Magnitude > 15 then
                                SmartTeleport(CFrame.new(pPos + Vector3.new(0, 3, 4)))
                            else
                            S.currentDungeonIsland = islandInfo.Folder
                                hrp.CFrame = CFrame.lookAt(pPos + Vector3.new(0, 3, 4), pPos)
                                hrp.Velocity = Vector3.zero
                                
                                local nc = RunService.Stepped:Connect(function()
                                    if char then for _, v in pairs(char:GetDescendants()) do if v:IsA("BasePart") then v.CanCollide = false end end end
                                end)
                                pcall(function()
                                    prompt.RequiresLineOfSight = false
                                    prompt.MaxActivationDistance = 50
                                    local cam = workspace.CurrentCamera
                                    if cam then cam.CFrame = CFrame.lookAt(cam.CFrame.Position, pPos) end
                                    task.wait(0.1)
                                    prompt:InputHoldBegin()
                                    task.wait(0.6) -- Securely bypasses the server's 0.5s strict hold check
                                    prompt:InputHoldEnd()
                                    if fireproximityprompt then fireproximityprompt(prompt, 1, true) end
                                end)
                                
                                local waitTime = 0
                                while not S.checkedDungeonPieces[islandInfo.Folder] and waitTime < 3 do
                                    task.wait(0.5)
                                    waitTime = waitTime + 0.5
                                end
                                if nc then nc:Disconnect() end
                            
                            local attempts = char:GetAttribute("PieceAttempts_" .. islandInfo.Folder) or 0
                            char:SetAttribute("PieceAttempts_" .. islandInfo.Folder, attempts + 1)
                            
                            if not prompt.Enabled or not prompt.Parent or attempts >= 4 then S.checkedDungeonPieces[islandInfo.Folder] = true end
                            end
                        else 
                            S.checkedDungeonPieces[islandInfo.Folder] = true 
                        end
                    end
                    
                    break -- Process one island piece completely before moving to next
                end
                
                if allDone then UpdateStatus("DUNGEON: All pieces collected!", C.Green); S.autoDungeonDiscovery = false end
            end
        end
        task.wait(1)
    end
end)

-- 8. AUTO DUNGEON EXECUTION LOOP
task.spawn(function()
    while _G.JomHubRunning do
        if S.autoDungeon then
            if game.PlaceId == 77747658251236 then
                if RequestDungeonPortal then pcall(function() RequestDungeonPortal:FireServer(S.dungeonType) end) end
            elseif game.PlaceId == 75159314259063 or game.PlaceId == 99684056491472 or game.PlaceId == 123955125827131 or game.PlaceId == 96767841099256 or game.PlaceId == 138368689293913 then
                if DungeonWaveVote then
                    if game.PlaceId == 138368689293913 or S.dungeonType == "InfiniteTower" then
                        pcall(function() DungeonWaveVote:FireServer("start") end)
                    else
                        pcall(function() DungeonWaveVote:FireServer(S.dungeonDiff) end)
                    end
                end
            end
        end
        task.wait(5)
    end
end)

-- 9. AUTO HOGYOKU DISCOVERY LOOP
task.spawn(function()
    local HOGYOKU_FRAGMENTS = {
        { Name = "HogyokuFragment1", Portal = "Snow" },
        { Name = "HogyokuFragment2", Portal = "Shibuya" },
        { Name = "HogyokuFragment3", Portal = "HollowIsland" },
        { Name = "HogyokuFragment4", Portal = "Shinjuku" },
        { Name = "HogyokuFragment5", Portal = "Slime" },
        { Name = "HogyokuFragment6", Portal = "Judgement" }
    }
    
    while _G.JomHubRunning do
        if S.autoHogyoku then
            local char = LocalPlayer.Character
            local hrp = char and char:FindFirstChild("HumanoidRootPart")
            if hrp then
                if QuestAccept and not S.hogyokuQuestAccepted then
                    UpdateStatus("HOGYOKU: Accepting Quest Remotely...", C.Cyan)
                    pcall(function() QuestAccept:FireServer("HogyokuQuestNPC") end)
                    S.hogyokuQuestAccepted = true
                    task.wait(1)
                end
                
                local allDone = true
                for _, fragInfo in ipairs(HOGYOKU_FRAGMENTS) do
                    if not S.autoHogyoku then break end
                    if S.checkedHogyokuPieces[fragInfo.Name] then continue end
                    
                    allDone = false
                    local piece = Workspace:FindFirstChild(fragInfo.Name)
                    
                    if not piece then
                        for _, v in ipairs(Workspace:GetDescendants()) do
                            if v.Name == fragInfo.Name then piece = v; break end
                        end
                    end
                    
                    if not piece then
                        if S.lastPortal ~= fragInfo.Portal then
                            if TeleportToPortal then
                                UpdateStatus("HOGYOKU: Teleporting to " .. fragInfo.Portal .. "...", C.Cyan)
                                executePortalTeleport(fragInfo.Portal)
                                S.lastPortal = fragInfo.Portal
                                task.wait(3)
                                break 
                            end
                        else
                            local attempts = char:GetAttribute("HogLoadAttempts_" .. fragInfo.Name) or 0
                            if attempts < 4 then
                                UpdateStatus("HOGYOKU: Searching for " .. fragInfo.Name .. "...", C.Cyan)
                                char:SetAttribute("HogLoadAttempts_" .. fragInfo.Name, attempts + 1)
                                task.wait(1.5)
                                break
                            else
                                S.checkedHogyokuPieces[fragInfo.Name] = true
                            end
                        end
                    else
                        local prompt = piece:FindFirstChild("HogyokuCollectPrompt", true) or piece:FindFirstChildWhichIsA("ProximityPrompt", true)
                        if prompt and prompt.Enabled then
                            UpdateStatus("HOGYOKU: Collecting " .. fragInfo.Name, C.Green)
                            local targetPart = prompt.Parent:IsA("BasePart") and prompt.Parent or piece
                            local pPos = targetPart:IsA("Model") and targetPart:GetPivot().Position or targetPart.Position
                            pcall(function() LocalPlayer:RequestStreamAroundAsync(pPos) end)
                            
                            if (hrp.Position - pPos).Magnitude > 15 then
                                SmartTeleport(CFrame.new(pPos + Vector3.new(0, 3, 4)))
                            else
                                hrp.CFrame = CFrame.lookAt(pPos + Vector3.new(0, 3, 4), pPos)
                                hrp.Velocity = Vector3.zero
                                S.currentHogyokuPiece = fragInfo.Name
                                
                                local nc = RunService.Stepped:Connect(function()
                                    if char then for _, v in pairs(char:GetDescendants()) do if v:IsA("BasePart") then v.CanCollide = false end end end
                                end)
                                
                                pcall(function() 
                                    prompt.RequiresLineOfSight = false
                                    prompt.MaxActivationDistance = 50
                                    local cam = workspace.CurrentCamera
                                    if cam then cam.CFrame = CFrame.lookAt(cam.CFrame.Position, pPos) end
                                    task.wait(0.1)
                                    prompt:InputHoldBegin()
                                    task.wait(0.6) -- Securely bypasses the server's 0.5s strict hold check
                                    prompt:InputHoldEnd()
                                    if fireproximityprompt then fireproximityprompt(prompt, 1, true) end
                                end)
                                
                                local waitTime = 0
                                while not S.checkedHogyokuPieces[fragInfo.Name] and waitTime < 3 do
                                    task.wait(0.5)
                                    waitTime = waitTime + 0.5
                                end
                                if nc then nc:Disconnect() end
                                
                                local attempts = char:GetAttribute("HogyokuAttempts_" .. fragInfo.Name) or 0
                                char:SetAttribute("HogyokuAttempts_" .. fragInfo.Name, attempts + 1)
                                if not prompt.Enabled or not prompt.Parent or attempts >= 4 then S.checkedHogyokuPieces[fragInfo.Name] = true end
                            end
                        else S.checkedHogyokuPieces[fragInfo.Name] = true end
                    end
                    break
                end
                if allDone then UpdateStatus("HOGYOKU: All fragments collected!", C.Green); S.autoHogyoku = false end
            end
        end
        task.wait(1)
    end
end)

-- 10. AUTO SLIME DISCOVERY LOOP
task.spawn(function()
    local SLIME_ISLANDS = {
        { Folder = "DesertIsland", Portal = "Desert" },
        { Folder = "SnowIsland", Portal = "Snow" },
        { Folder = "StarterIsland", Portal = "Starter" },
        { Folder = "JungleIsland", Portal = "Jungle" },
        { Folder = "ShibuyaStation", Portal = "Shibuya" },
        { Folder = "HollowIsland", Portal = "HollowIsland" },
        { Folder = "ShinjukuIsland", Portal = "Shinjuku" }
    }
    
    while _G.JomHubRunning do
        if S.autoSlimeDiscovery then
            local char = LocalPlayer.Character
            local hrp = char and char:FindFirstChild("HumanoidRootPart")
            if hrp then
                if QuestAccept and not S.slimeQuestAccepted then
                    pcall(function() QuestAccept:FireServer("SlimeCraftNPC") end)
                    S.slimeQuestAccepted = true
                    task.wait(0.5)
                end
                
                local allDone = true
                for _, islandInfo in ipairs(SLIME_ISLANDS) do
                    if not S.autoSlimeDiscovery then break end
                    if S.checkedSlimePieces[islandInfo.Folder] then continue end
                    
                    allDone = false
                    local islandFolder = Workspace:FindFirstChild(islandInfo.Folder)
                    local piece = islandFolder and islandFolder:FindFirstChild("SlimePuzzlePiece")
                    
                    if not piece then
                        if S.lastPortal ~= islandInfo.Portal then
                            if TeleportToPortal then
                                UpdateStatus("SLIME: Teleporting to " .. islandInfo.Portal .. "...", C.Cyan)
                                executePortalTeleport(islandInfo.Portal)
                                S.lastPortal = islandInfo.Portal
                                task.wait(3)
                                break
                            end
                        else
                            local attempts = char:GetAttribute("SlimeLoadAttempts_" .. islandInfo.Folder) or 0
                            if attempts < 4 then
                                char:SetAttribute("SlimeLoadAttempts_" .. islandInfo.Folder, attempts + 1)
                                task.wait(1.5)
                                break
                            else
                                S.checkedSlimePieces[islandInfo.Folder] = true
                            end
                        end
                    else
                        local prompt = piece:FindFirstChildWhichIsA("ProximityPrompt", true)
                        if prompt and prompt.Enabled then
                            UpdateStatus("SLIME: Collecting piece at " .. islandInfo.Folder, C.Green)
                            local targetPart = prompt.Parent:IsA("BasePart") and prompt.Parent or piece
                            local pPos = targetPart:IsA("Model") and targetPart:GetPivot().Position or targetPart.Position
                            pcall(function() LocalPlayer:RequestStreamAroundAsync(pPos) end)
                            
                            if (hrp.Position - pPos).Magnitude > 15 then
                                SmartTeleport(CFrame.new(pPos + Vector3.new(0, 3, 4)))
                            else
                                hrp.CFrame = CFrame.lookAt(pPos + Vector3.new(0, 3, 4), pPos)
                                hrp.Velocity = Vector3.zero
                                
                                local nc = RunService.Stepped:Connect(function()
                                    if char then for _, v in pairs(char:GetDescendants()) do if v:IsA("BasePart") then v.CanCollide = false end end end
                                end)
                                pcall(function()
                                    prompt.RequiresLineOfSight = false
                                    prompt.MaxActivationDistance = 50
                                    local cam = workspace.CurrentCamera
                                    if cam then cam.CFrame = CFrame.lookAt(cam.CFrame.Position, pPos) end
                                    task.wait(0.1)
                                    prompt:InputHoldBegin()
                                    task.wait(0.6)
                                    prompt:InputHoldEnd()
                                    if fireproximityprompt then fireproximityprompt(prompt, 1, true) end
                                end)
                                
                                task.wait(1.5)
                                if nc then nc:Disconnect() end
                                S.checkedSlimePieces[islandInfo.Folder] = true
                            end
                        else 
                            S.checkedSlimePieces[islandInfo.Folder] = true 
                        end
                    end
                    break
                end
                
                if allDone then UpdateStatus("SLIME: All pieces collected!", C.Green); S.autoSlimeDiscovery = false end
            end
        end
        task.wait(1)
    end
end)

-- 11. AUTO DEMONITE DISCOVERY LOOP
task.spawn(function()
    local DEMONITE_FRAGMENTS = {
        { Name = "DemoniteCore1" },
        { Name = "DemoniteCore2" }
    }
    
    while _G.JomHubRunning do
        if S.autoDemonite then
            local char = LocalPlayer.Character
            local hrp = char and char:FindFirstChild("HumanoidRootPart")
            if hrp then
                if QuestAccept and not S.demoniteQuestAccepted then
                    pcall(function() QuestAccept:FireServer("AnosQuestNPC") end)
                    S.demoniteQuestAccepted = true
                    task.wait(1)
                end
                
                local allDone = true
                for _, fragInfo in ipairs(DEMONITE_FRAGMENTS) do
                    if not S.autoDemonite then break end
                    if S.checkedDemonitePieces[fragInfo.Name] then continue end
                    
                    allDone = false
                    local piece = Workspace:FindFirstChild(fragInfo.Name)
                    if not piece then
                        for _, v in ipairs(Workspace:GetDescendants()) do
                            if v.Name == fragInfo.Name then piece = v; break end
                        end
                    end
                    
                    if not piece then
                        UpdateStatus("DEMONITE: Searching map for " .. fragInfo.Name .. "...", C.Cyan)
                        task.wait(2)
                        break
                    else
                        local prompt = piece:FindFirstChildWhichIsA("ProximityPrompt", true)
                        if prompt and prompt.Enabled then
                            UpdateStatus("DEMONITE: Collecting " .. fragInfo.Name, C.Green)
                            local targetPart = prompt.Parent:IsA("BasePart") and prompt.Parent or piece
                            local pPos = targetPart:IsA("Model") and targetPart:GetPivot().Position or targetPart.Position
                            pcall(function() LocalPlayer:RequestStreamAroundAsync(pPos) end)
                            
                            if (hrp.Position - pPos).Magnitude > 15 then
                                SmartTeleport(CFrame.new(pPos + Vector3.new(0, 3, 4)))
                            else
                                hrp.CFrame = CFrame.lookAt(pPos + Vector3.new(0, 3, 4), pPos)
                                hrp.Velocity = Vector3.zero
                                
                                local nc = RunService.Stepped:Connect(function()
                                    if char then for _, v in pairs(char:GetDescendants()) do if v:IsA("BasePart") then v.CanCollide = false end end end
                                end)
                                
                                pcall(function() 
                                    prompt.RequiresLineOfSight = false
                                    prompt.MaxActivationDistance = 50
                                    local cam = workspace.CurrentCamera
                                    if cam then cam.CFrame = CFrame.lookAt(cam.CFrame.Position, pPos) end
                                    task.wait(0.1)
                                    prompt:InputHoldBegin()
                                    task.wait(0.6)
                                    prompt:InputHoldEnd()
                                    if fireproximityprompt then fireproximityprompt(prompt, 1, true) end
                                end)
                                
                                task.wait(1.5)
                                if nc then nc:Disconnect() end
                                S.checkedDemonitePieces[fragInfo.Name] = true
                            end
                        else S.checkedDemonitePieces[fragInfo.Name] = true end
                    end
                    break
                end
                if allDone then UpdateStatus("DEMONITE: All fragments collected!", C.Green); S.autoDemonite = false end
            end
        end
        task.wait(1)
    end
end)

-- 5. DUNGEON FARM LOOP
task.spawn(function()
    
    while _G.JomHubRunning do
        local isInDungeonEnv = (game.PlaceId == 75159314259063 or game.PlaceId == 99684056491472 or game.PlaceId == 123955125827131 or game.PlaceId == 96767841099256 or game.PlaceId == 138368689293913)
        local isDungeonFarm = S.autoDungeon and isInDungeonEnv
        
        if isDungeonFarm and NPCs then
            local char = LocalPlayer.Character
            local hrp = char and char:FindFirstChild("HumanoidRootPart")
            local hum = char and char:FindFirstChild("Humanoid")
            
            if hrp and hum and hum.Health > 0 then
                local allEnemies = {}
                
                for _, npc in ipairs(NPCs:GetChildren()) do
                    local nHum = npc:FindFirstChild("Humanoid")
                    local nRoot = npc:FindFirstChild("HumanoidRootPart")
                    if nHum and nHum.Health > 0 and nRoot then
                        table.insert(allEnemies, nRoot)
                    end
                end
                
                if #allEnemies > 0 then
                    local wantsAura = (S.dungeonFarmMode == "Kill Aura")
                    local useAura = wantsAura and (#allEnemies >= 2)
                    
                    if useAura then
                        hum:SetStateEnabled(Enum.HumanoidStateType.Ragdoll, false)
                        hum:SetStateEnabled(Enum.HumanoidStateType.FallingDown, false)
                        if char:GetAttribute("DungFloatLocked") then
                            char:SetAttribute("DungFloatLocked", nil)
                        end
                        
                        local minDist = math.huge
                        local closestRoot = nil
                        for _, root in ipairs(allEnemies) do
                            local dist = (hrp.Position - root.Position).Magnitude
                            if dist < minDist then
                                minDist = dist
                                closestRoot = root
                            end
                        end
                        local mainTargetRoot = closestRoot or allEnemies[1]
                        
                        local goalCF = getFarmPosition(mainTargetRoot, S.farmDistance, S.dungeonPos)
                        
                        if (hrp.Position - goalCF.Position).Magnitude > 50 then
                            SmartTeleport(goalCF)
                        else
                            local fbv = hrp:FindFirstChild("JomFarmBV")
                            if not fbv then
                                fbv = Instance.new("BodyVelocity"); fbv.Name = "JomFarmBV"; fbv.MaxForce = Vector3.new(math.huge, math.huge, math.huge); fbv.Velocity = Vector3.zero; fbv.Parent = hrp
                            end
                            for _, v in ipairs(hrp:GetChildren()) do
                                if (v:IsA("BodyVelocity") and v.Name ~= "JomFarmBV" and v.Name ~= "JomTeleportBV") or v:IsA("BodyPosition") or v:IsA("LinearVelocity") or v:IsA("AlignPosition") then
                                    v:Destroy()
                                end
                            end
                            if (hrp.Position - goalCF.Position).Magnitude > 5 then
                                hrp.CFrame = goalCF
                            end
                            hrp.Velocity = Vector3.zero
                        end
                        
                        local originalPos = hrp.CFrame
                        local enemiesToHit = {}
                        
                        for _, nRoot in ipairs(allEnemies) do
                            if nRoot:IsDescendantOf(workspace) and (originalPos.Position - nRoot.Position).Magnitude < 400 then
                                table.insert(enemiesToHit, nRoot)
                                pcall(function()
                                    local nHum = nRoot.Parent:FindFirstChild("Humanoid")
                                    if nHum then nHum.WalkSpeed = 0; nHum.JumpPower = 0 end
                                    nRoot.CanCollide = false
                                    nRoot.Velocity = Vector3.zero
                                end)
                                if #enemiesToHit >= 15 then break end
                            end
                        end
                        
                        if #enemiesToHit > 0 then
                            local weapons = getWeaponsToUse()
                            if #weapons == 0 then weapons = {nil} end
                            
                            for _, wep in ipairs(weapons) do
                                if wep and wep.Parent ~= char then
                                    pcall(function() hum:EquipTool(wep) end)
                                    task.wait(0.05)
                                end
                                
                                for _, nRoot in ipairs(enemiesToHit) do
                                    if nRoot and nRoot.Parent then
                                        hrp.CFrame = getFarmPosition(nRoot, S.farmDistance, S.dungeonPos)
                                        hrp.Velocity = Vector3.zero
                                        task.wait(0.02)
                                        
                                        for _ = 1, 2 do
                                            if RequestHit then pcall(function() RequestHit:FireServer(nRoot.Parent) end) end
                                            if CombatRemote then pcall(function() CombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                            if KatanaCombatRemote then pcall(function() KatanaCombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                        end
                                        
                                        if RequestAbility then
                                            if S.autoSkills["Skill Z"] then pcall(function() RequestAbility:FireServer(1) end) end
                                            if S.autoSkills["Skill X"] then pcall(function() RequestAbility:FireServer(2) end) end
                                            if S.autoSkills["Skill C"] then pcall(function() RequestAbility:FireServer(3) end) end
                                            if S.autoSkills["Skill V"] then pcall(function() RequestAbility:FireServer(4) end) end
                                            if S.autoSkills["Skill F"] then pcall(function() RequestAbility:FireServer(5) end) end
                                        end
                                    end
                                end
                            end
                            
                            hrp.CFrame = originalPos
                            hrp.Velocity = Vector3.zero
                        end
                    else
                        -- SINGLE TARGET DUNGEON MODE
                        if char:GetAttribute("DungFloatLocked") then
                            char:SetAttribute("DungFloatLocked", nil)
                        end
                        local fbv = hrp:FindFirstChild("JomFarmBV")
                        if fbv then fbv:Destroy() end
                        
                        local minDist = math.huge
                        local closestRoot = nil
                        for _, root in ipairs(allEnemies) do
                            local dist = (hrp.Position - root.Position).Magnitude
                            if dist < minDist then
                                minDist = dist
                                closestRoot = root
                            end
                        end
                        local nRoot = closestRoot or allEnemies[1]
                        
                        pcall(function()
                            local nHum = nRoot.Parent:FindFirstChild("Humanoid")
                            if nHum then nHum.WalkSpeed = 0; nHum.JumpPower = 0 end
                            nRoot.CanCollide = false
                            nRoot.Velocity = Vector3.zero
                        end)
                        
                        local goalCF = getFarmPosition(nRoot, S.farmDistance, S.dungeonPos)
                        
                        if (hrp.Position - goalCF.Position).Magnitude > 50 then
                            SmartTeleport(goalCF)
                        else
                            local fbv2 = hrp:FindFirstChild("JomFarmBV")
                            if not fbv2 then
                                fbv2 = Instance.new("BodyVelocity"); fbv2.Name = "JomFarmBV"; fbv2.MaxForce = Vector3.new(math.huge, math.huge, math.huge); fbv2.Velocity = Vector3.zero; fbv2.Parent = hrp
                            end
                            for _, v in ipairs(hrp:GetChildren()) do
                                if (v:IsA("BodyVelocity") and v.Name ~= "JomFarmBV" and v.Name ~= "JomTeleportBV") or v:IsA("BodyPosition") or v:IsA("LinearVelocity") or v:IsA("AlignPosition") then
                                    v:Destroy()
                                end
                            end
                            
                            if (hrp.Position - goalCF.Position).Magnitude > 5 then
                                hrp.CFrame = goalCF
                            end
                            hrp.Velocity = Vector3.zero
                            
                            if not char:GetAttribute("LastAttack") or tick() - char:GetAttribute("LastAttack") > math.max(0.1, 0.5 / S.fastAttack) then
                                char:SetAttribute("LastAttack", tick())
                                
                                local weapons = getWeaponsToUse()
                                if #weapons == 0 then weapons = {nil} end
                                
                                for _, wep in ipairs(weapons) do
                                    if wep and wep.Parent ~= char then
                                        pcall(function() hum:EquipTool(wep) end)
                                        task.wait(0.05)
                                    end
                                    
                                    for _ = 1, 2 do
                                        if RequestHit then pcall(function() RequestHit:FireServer(nRoot.Parent) end) end
                                        if CombatRemote then pcall(function() CombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                        if KatanaCombatRemote then pcall(function() KatanaCombatRemote:FireServer("Punch", nRoot.Parent) end) end
                                    end
                                    
                                    if RequestAbility then
                                        if S.autoSkills["Skill Z"] then pcall(function() RequestAbility:FireServer(1) end) end
                                        if S.autoSkills["Skill X"] then pcall(function() RequestAbility:FireServer(2) end) end
                                        if S.autoSkills["Skill C"] then pcall(function() RequestAbility:FireServer(3) end) end
                                        if S.autoSkills["Skill V"] then pcall(function() RequestAbility:FireServer(4) end) end
                                        if S.autoSkills["Skill F"] then pcall(function() RequestAbility:FireServer(5) end) end
                                    end
                                end
                            end
                        end
                    end
                end
            end
        end
        task.wait()
        if not isDungeonFarm then
            local char = LocalPlayer.Character
            local hrp = char and char:FindFirstChild("HumanoidRootPart")
            if hrp then
                if char:GetAttribute("DungFloatLocked") then
                    char:SetAttribute("DungFloatLocked", nil)
                    local fbv = hrp:FindFirstChild("JomFarmBV")
                    if fbv then fbv:Destroy() end
                end
            end
        end
    end
end)

-- 6. AUTO OPEN CHEST LOOP
task.spawn(function()
    while _G.JomHubRunning do
        if S.autoOpenChests and UseItem then
            for chestName, isSelected in pairs(S.chestsToOpen) do
                if isSelected then
                    local hasChest = true
                    if next(InventoryCache) ~= nil then 
                        if not InventoryCache[chestName] or InventoryCache[chestName] <= 0 then hasChest = false end
                    end
                    
                    if hasChest then
                        pcall(function() UseItem:FireServer("Use", chestName, 1, false) end)
                        task.wait(0.6) -- Respects the server's 0.5s DefaultDelay anti-cheat
                    end
                end
            end
        end
        task.wait(1)
    end
end)
