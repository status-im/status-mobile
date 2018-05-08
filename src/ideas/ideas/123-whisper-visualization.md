## Preamble

    Idea: 123
    Title: Whisper Visualization
    Status: Draft
    Created: 2018-04-06

## Summary
WebGL 3D visualization platform for Whisper and peer-to-peer processes (message propagation, peers discovery, etc), both for educational and research purposes.

![demo1](https://i.imgur.com/MZElq0K.png)

*Note: current image doesn't represent real Status cluster (yet) and serves only for showcasing purposes.*

[Demo GIF](https://i.imgur.com/GAu4zte.gifv)

The core aspect of Status messaging layer is a peer-to-peer (p2p) network of Ethereum nodes and the Whisper messaging protocol. For developers it is crucial to understand how P2P overlay network and Whisper works and build intuition about it. However, P2P networks in general are among one of the most complicated topics to study.

The decentralized nature of those networks increase security and privacy (which is exactly the reason why they are built), but makes it incredibly hard to collect meaningful metrics, understand network topology, analyze its behaviour and create educational material. This results in a generaly poor understanding of P2P decentralized systems, and that's exactly the problem this project aims to attack.

Proposed visualization framework should provide a tooling for visualizing different types and scales of Ethereum network, processes that occur in network in different layers (p2p, les, whisper, etc), both collected from real data and from simulations.

## Swarm Participants
- Lead Contributor: [@divan](https://github.com/divan)
- Testing & Evaluation: 
- Contributor:
- Contributor:
- PM: 
- UX: 

## Product Overview

![demo2](https://i.imgur.com/9IajeMC.png)

*Note: current image doesn't represent real Status cluster (yet) and serves only for showcasing purposes.*

Essentially the product is a software framework, that serves as a platform for building end-user products. One project in particular is suggested to be a showcase of the potential and a starting point for other projects – visualization of the Whisper message propagation in the Status cluster.

Other projects that can be implemented using this framework:

- real-time visual monitoring of Status cluster - visualizing nodes connectivity, CPU/memory/disk utilization, ingress/egress traffic, etc
- visualizing real Status app activity by collecting opt-in metrics from Status app
- researching the network heterogeneity and clusterization for Status nodes with different peers layer configurations
- educational visualizations of Whisper messaging, P2P network types and their properties

It's impossible to predict all possible ideas and needs, so the framework should provide easy to use building blocks, rather than try to be an all-in-one hyper-configurable solution. 

Basically there are two important parts in any of the visualization projects:

##### User interface part (frontend)

The P2P network is essentially a directed graph, and the best known approach to represent those graphs is to use force-directed graph layout. It allows human brain to reuse our intuition built on top of physics and understand graph properties intuitively. The algorithm is basically to put peers (nodes) into 3D space, assign some weight (mass) to them and apply two physical forces: *repelling force* between nodes and *spring force* between linked nodes. The forces than applied repeatedly till the system stabilze i.e. reach the minimum energy threshold.

- WebGL 3D representation of p2p network and processes
- UI for user interaction: controls, import/export data, restarting or stepping through simulation, etc

##### Data collection part

Data collectors can be implemented separately once the common data exchange format is determined. Currently JSON format is used. Data can be collected or generated in many different ways:

- metrics from the controlled cluster and e2e tests
- metrics gathered from the Ethereum crawlers bots
- message propagation simulations from P2P network simulators
- empirical data based on state of the art estimation techniques (for example, BMS system described in https://arxiv.org/pdf/1801.03998.pdf)

---

User interface part needs to be implemented in the browser, because we want to reach as many people as possible, and thus have to use web. Fortunately, most browsers got decent support of WebGL technology and even have native GPU rendering capabilities. Currently the fantastic library [Three.js](https://threejs.org) is used – it has smooth learning curve, hides all shaders magic, provides nice API and great documentation.

The downside of implementing this part for web is, of course, inherit limitation of web browsers in terms of supported languages – they support 0 programming languages plus Javascript. WebAssembly is not a thing yet, but we still have to implement some important calculations for this part, like physical model for the force-directed graph layout and Barne-Hut optimizations, so the suggested approach is to offload most of heavy calculation to the backend server running on the same machine, that communicates with frontend via websockets. This approach proved to be fast enough in terms of responsiveness for the set of tasks needed for this part, and allows to implement pretty complicated concurrent calculations that run natively on the user machine, and not in the browser.

##### Data processing part (backend)

Currently most of the code involved into data processing is written as separate web servers that talks to the frontend via websockets. Important parts are:

- force-oriented graph physical model
- common data format import/export code
- use-case specific data processing and optimization for frontend
- websocket server

As we're talking about set of tools and libraries, all data collectors and generators could be implemented as a part of backend as well, adding to the interactivity of the final product. For example, user interface can allow changing different options for the simulations or data collectors and rerun them directly from the browser.

## Product Details

#### Help needed

The core part of this project is already written and will be opened this week. There three major focus points where brains and skills are needed:

- Frontend developers: frontend part still have to be written in Javascript, so JS-ninjas are very welcome. My goal is to package current [Vanilla.js](http://vanilla-js.com) code into something easy to work with and refactor in the future - React for example (suggestions are welcomed).
- UI/UX/VR designers: as this project involves user interactivity why not make it bloody awesome? This is virtual 3D space and we can do whatever we want – change colors, shapes, invent better way of visualization of complex systems, put this into VR space, anything. Maybe you have ideas for #ArtProject and want to make physical model out of the visualization. Let's talk.
- Researchers: mainly Go developers who involved into P2P-layer, nodes discovery and Whisper protocol improvements and research. Most of the simulations can be done without visualization, but having visual model helps enormously to develop intution and stir research tools into the right direction.
- WebGL/shaders ninjas: if you, by any chance, have an experience with [GLSL](https://en.wikipedia.org/wiki/OpenGL_Shading_Language) (OpenGL Shading Language), you're are very welcomed. Larger graphs and cooler animations will require bringing more shaders programming magic, and I will really need help here.

#### Performance considerations

It's important to realize that in WebGL world performance is an important player when it comes to the tradeoffs. Graphs with 10, 100, 1000 and 1000000 nodes require drastically different approaches. Static nodes vs dynamic nodes rendering should be implemented in a totally different way, so for each particular project, this should be taken into account.

Whisper message propagation project should work relatively fast with a graphs ~1K nodes. For higher numbers more optimizations have to be made.

### Requirements & Dependencies
This idea may be related to the [92 - Research Ethereum Discovery V5 Protocol](https://github.com/status-im/ideas/blob/master/ideas/92-disc-v5-research.md) idea.

### Minimum Viable Product
Goal Date: 06-05-2018

Description: The Whisper propagation visualization tool is implemented and documented. It should use either simulated propagation data or real data from controlled cluster. Minimal needed set of interactivity and controls is implemented (ability to choose node initiating the message sending, start multiple message sendings, etc). Code (libraries and tools) is documented and prepared to be open-sourced and used by third-party contributors.

## Dates
Goal Date: 06-06-2018

Description:  First visualization project from another teams (i.e. Marketing, Community etc) or external contributors are implemented. That may be creating educational material on messaging in Whisper, researching libp2p code behaviour, visualizing Mailboxes, etc.

## Success Metrics
1. There is at least three useful projects that use this framework for different goals (i.e. research, monitoring, education).
2. Once open-sourced, number of "Awesome" comments should be at least 10 per week.

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
