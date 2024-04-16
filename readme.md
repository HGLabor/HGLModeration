# Table of Contents
- ###[Requirements](#requirements)
- ###[Permissions](#permissions)
- ###[Config](#config)
- ###[Presets & Weight](#weight-&-presets)
- ###[Fetcher](#fetcher)

## Requirements

---

- SignedVelocity for Proxy and Paper: https://modrinth.com/plugin/signedvelocity
- LuckPerms for Proxy: https://github.com/LuckPerms/LuckPerms

## Permissions

--- 
### LuckPerms Weight
In order to use this plugin, you have to configure your LuckPerms groups with weight. 
That way the Moderation plugin can detect, if a user has a greater role (hierarchical). 
Otherwise you might run into issues such as lower rank players (e. g. Supporters) banning higher rank players (e. g. Admins).

### Plugin Permissions
```yaml
    hglmoderation:
        fetcher
        link
        moderation:
            notifications
        notification
        punish
        report
        review
        unpunish
```
### Notification Note
In order to receive notifications on report a user must be inside the Report notification group.
They can ONLY JOIN THE GROUP IF THEY HAVE THE **hglmoderation.moderation.notifications** permission.
Make sure to add this permission to your server team.

## Config

---
The config consists of 2 parts mainly. The discord part and the initial config file.

### Config File
The config file is first created when starting the plugin.
As the file extension already suggests it requires the configuration to be in a json format. 
The least amount of config needed in order to operate this plugin is:
```json
{
  "dbConnectionString": "YOUR_MONGODB_CONNECTION_STRING",
  "discordBotToken": "YOUR_BOT_TOKEN",
  "reportChannelId": "",
  "punishmentChannelId": "",
  "reportPingroleId": ""
}
```
### Discord
On discord, you don't necessarily have to configure anything.
However, in order to unfold this plugins full potential you might want to add a reportChannelId to the config
and a punishmentChannelId. Don't worry you don't have to go back to the config file. 
You can simply use the following commands to set the channel ids:
``/logs set <type> <channel>``

#### Logs Type:
- punishment
- report

By setting these log channels, you will be notified when a new report or punishment has been submitted.

## Weight & Presets

---

### Weight
In order to understand what exactly weight refers to, you need to know that internally we keep track of a users
"punishment score". Why is this relevant? Well it is relevant to predetermine a players punishment and also
treat all players equally. By punishing a player, we automatically add punishments weight value to the existing
punishment score. 

By tracking a players punishment score we ensure the following points:
- Fairness
- Less random Bans
- Ban Templates

### Presets
Speaking of ban templates. Whenever we talk about Presets we actually
mean ban templates so please keep that in mind. How do you actually configure one?

In order to configure a preset you can simply head over to discord and type in the following command ``/preset``.
You should be greeted with a welcoming GUI that shows all of your currently active presets. Honestly
there isn't much to say about presets, they are pretty self-explanatory.

#### Most important Information:
- Preset start -> Lower bound of a presets range including this number (punishmentScore >= presetStart)
- Preset end -> Upper bound of a presets range including this number (punishmentScore <= presetEnd)
- Scope -> Reasoning Scope of a preset (Mute preset for X punishment score should only be active for Chat reasons)
- PunishmentType -> On a technical scale you could have multiple different punishments active,
therefore you have to declare the types of a preset to punish a player accordingly
- Weight -> The weight added to a players punishment score when this preset is applied

## Fetcher

---

The fetcher is a powerful tool. Not only can you retrieve currently open and under_review reports,
but also all kinds of data that is stored with this plugin.

### Fetchable Types:
- Reports
- PlayerData
- Punishments

### Usage
The basic usage is always ``/fetcher <type> <id|username|under_review>``

### Output
Fetcher always retrieves its most recent data if nothing specific is clarified. 
Therefore, if you use ``/fetcher <type>`` it will actually return either your specific PlayerData
or your most recent Punishment or all open reports. As I said the Fetcher is smart.

#### Output summary

``/fetcher report`` -> Returns all currently open Reports

``/fetcher report under_review`` -> Returns all Reports, that are currently under review

``/fetcher report <id>`` -> Returns details for specific Report

``/fetcher report <username>`` -> Returns all active Reports for a player

``/fetcher player_data`` -> Returns your Player Data

``/fetcher player_data <username>`` -> Returns Player Data for a specific player

``/fetcher punishment`` -> Returns your most recent Punishment

``/fetcher punishment <username>`` -> Returns a players most recent Punishment

``/fetcher punishment <id>`` -> Returns details about a specific Punishment