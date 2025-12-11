# Auctioneer

[![GitHub Release](https://img.shields.io/github/v/release/Garsooon/Billboard?label=release)](https://github.com/Garsooon/Billboard/releases/latest)  
[![Downloads](https://img.shields.io/github/downloads/Garsooon/Billboard/total.svg?style=flat)](https://github.com/Garsooon/Billboard/releases)  
[![GitHub Stars](https://img.shields.io/github/stars/Garsooon/Billboard?style=social)](https://github.com/Garsooon/Billboard/stargazers)

**Billboard** is a simple chat-based advertisement plugin for Minecraft Beta 1.7.3.

---

## Features

- Start auctions with `/auction <price>`
- Live bidding through chat
- Configurable auction time, bid time increases and item blacklist
- Optional minimum bid increase (after first bid)

---

## Requirements

- [Project Poseidon](https://github.com/retromcorg/Project-Poseidon)

---

## Installation

1. Download `Billboard.jar` from the [releases](https://github.com/Garsooon/Billboard/releases).
2. Drop it into your `plugins` folder.</br>
3. Make sure [Poseidon](https://github.com/retromcorg/Project-Poseidon) is installed.
4. Restart your server.

---

## Usage

- **Request an AD**:  
  `/buyad <Duration> <message>`  
  Sends a request for an AD slot to moderators.</br>

- **Approving/Denying ads**:  
  `/reviewads`  
  This shows pending ads</br>
  `/approvead #` & `/denyad #`</br>
  This approves/denies an AD</br>
  `/listads`
  This shows all currently running ADs

---

## Configuration

The config file (`plugins/Billboard/config.yml`) lets you adjust:

- Broadcast header
- Broadcasting interval
- Max message length
- Max days

---

## Building

You can build the plugin using Maven:

```bash
mvn clean package
```
