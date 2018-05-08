# Username ENS subdomain Registrar

## Preamble

    Idea: 151-ens-usernames
    Title: Username ENS subdomain Registrar
    Status: Draft
    Created: 2017-11-20
    Replaces: 027-ens-usernames

## Summary

Eliminates the need to copy/scan - and worse, type - long hexadecimal addresses / public keys, by providing an [ENS](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-137.md) subdomain registry and recognition of ENS names in Status for interacting with other people in Status.

## Swarm Participants

- Lead Contributor: Ricardo Guilherme Schmidt
- Contributor: Richard Ramos
- UX (Mist): Alexandre Van de Sande 
- UX (Status): Hester Bruikman
- Design: Denis Sharypin
- PM: Rachel Hamlin

## Product Overview

A bad experience in most cryptosystems, aswell Status, is the requirement of typing or scanning the public keys and addresses to interact with other users in the network.
ENS disrupt centralized DNS: like DNS resolves into IPs, ENS resolve into ethereum addresses (and more) trustlessly.
Status would introduce a option for users registering a human readable contact username and its URI would be used in place of QR codes / private keys.
A small fee is required, so users are buying the subdomain, and therefore could be exchanged with other users, this network effect is ignored by this idea.

### Product Description

Users would be able to:
- buy a subdomain from one of the ENS domains owned by Status.
- find users by using the configured ENS Resolver for EMS address (subdomain.domain.eth)
- search/suggest domain for a username.
- send funds by typing their ENS address.

Subdomain owners would be able to:
- change the owner of the subdomain
- change the resolver of the subdomain

Democracy (or authority) would be able to:
- add new domains available with its own price
- change domains price
- change owner of domain (required for update of subdomian registry)

### Minimum Viable Product

Goal Date: 2018-05-10
Description: 

- Users can register (FIFS) an available username in Status's ENS subdomain
- Users can add friends by entering an ENS address
- Basic UI for call include a domain

## Success Metrics

Most users opt-in to register their username and use username subdomain to find their friends.

## Exit Criteria

Running on Mainnet 

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

