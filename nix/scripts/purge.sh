#!/usr/bin/env bash
# This script removes all Nix files.

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
source "${GIT_ROOT}/nix/scripts/lib.sh"
source "${GIT_ROOT}/scripts/colors.sh"

nix_purge_linux_multi_user_service() {
    NIX_SERVICES=(nix-daemon.service nix-daemon.socket)
    for NIX_SERVICE in "${NIX_SERVICES[@]}"; do
        sudo systemctl stop "${NIX_SERVICE}"
        sudo systemctl disable "${NIX_SERVICE}"
    done
    sudo systemctl daemon-reload
}

nix_purge_linux_multi_user_users() {
    for NIX_USER in $(awk -F: '/nixbld/{print $1}' /etc/passwd); do
        sudo userdel "${NIX_USER}"
    done
    sudo groupdel nixbld
}

nix_purge_darwin_multi_user_service() {
    cd /Library/LaunchDaemons
    NIX_SERVICES=(org.nixos.darwin-store.plist org.nixos.nix-daemon.plist)
    for NIX_SERVICE in "${NIX_SERVICES[@]}"; do
        sudo launchctl unload "${NIX_SERVICE}" || true
        sudo launchctl remove "${NIX_SERVICE}" || true
    done
}

nix_purge_darwin_multi_user_users() {
    for NIX_USER in $(dscl . list /Users | grep nixbld); do
        sudo dscl . -delete "/Users/${NIX_USER}"
    done
    sudo dscl . -delete /Groups/nixbld
}

# This still leaves an empty /nix, which will disappear after reboot.
nix_purge_darwin_multi_user_volumes() {
    sudo sed -i.bkp '/nix/d' /etc/synthetic.conf
    sudo sed -i.bkp '/nix/d' /etc/fstab

    # Attempt to delete the volume
    if ! sudo diskutil apfs deleteVolume /nix; then
        echo "Failed to unmount /nix because it is in use."

        # Identify the process using the volume
        local pid=$(lsof +D /nix | awk 'NR==2{print $2}')
        if [ -n "$pid" ]; then
            echo "The volume /nix is in use by process ID $pid."

            # Ask the user whether to terminate the process
            read -p "Do you want to terminate this process? (y/n): " choice
            if [[ $choice == "y" ]]; then
                sudo kill $pid
                echo "Process $pid terminated."
            else
                echo "Process not terminated. Please close it manually and retry."
                return 1
            fi
        else
            echo "No process found using /nix. Manual intervention required."
            return 1
        fi
    fi

    echo -e "${YLW}You will need to reboot your system!${RST}" >&2
}

nix_purge_multi_user() {
    if [[ $(uname -s) == "Darwin" ]]; then
        nix_purge_darwin_multi_user_service || true
        nix_purge_darwin_multi_user_users || true
        nix_purge_darwin_multi_user_volumes || true
    else
        nix_purge_linux_multi_user_service
        nix_purge_linux_multi_user_users
    fi

    sudo rm -fr /etc/nix
    sudo rm -f /etc/profile.d/nix.sh*

    # Restore old shell profiles
    NIX_PROFILE_FILES=(
        /etc/bash.bashrc /etc/bashrc /etc/bash/bashrc
        /etc/zsh.zshhrc /etc/zshrc /etc/zsh/zshrc
    )
    for NIX_FILE in "${NIX_PROFILE_FILES[@]}"; do
        if [[ -f "${NIX_FILE}.backup-before-nix" ]]; then
            sudo mv -f "${NIX_FILE}.backup-before-nix" "${NIX_FILE}"
        fi
    done
}

nix_purge_user_profile() {
    sudo rm -rf \
        ~/.nix-* \
        ~/.cache/nix \
        ~/.config/nixpkgs \
        "${GIT_ROOT}/.nix-gcroots"
}

nix_purge_root() {
    NIX_ROOT=$(nix_root)
    if [[ -z "${NIX_ROOT}" ]]; then
        echo -e "${RED}Unable to identify Nix root!${RST}" >&2
        exit 1
    fi
    sudo rm -fr "${NIX_ROOT}"
}

# Don't run anything if script is just sourced.
if (return 0 2>/dev/null); then
    echo -e "${YLW}Script sourced, not running purge.${RST}"
    return
fi

# Confirm user decission, unless --force is used.
if [[ "${1}" != "--force" ]]; then
    echo -e "${YLW}Are you sure you want to purge Nix?${RST}" >&2
    read -p "[y/n]: " -n 1 -r
    echo
    if [[ $REPLY =~ ^[^Yy]$ ]]; then
        echo -e "${GRN}Aborting Nix purge!${RST}" >&2
        exit 0
    fi
fi

NIX_INSTALL_TYPE=$(nix_install_type)
# Purging /nix on NixOS would be disasterous.
if [[ "${NIX_INSTALL_TYPE}" == "nixos" ]]; then
    echo -e "${RED}You should not purge Nix files on NixOS!${RST}" >&2
    exit
elif [[ "${NIX_INSTALL_TYPE}" == "none" ]] && [[ "${1}" != "--force" ]]; then
    echo -e "${YLW}Nothing to remove, Nix not installed.${RST}" >&2
    exit
elif [[ "${NIX_INSTALL_TYPE}" == "multi" ]] || [[ "${1}" == "--force" ]]; then
    echo -e "${YLW}Detected multi-user Nix installation.${RST}" >&2
    nix_purge_multi_user
elif [[ "${NIX_INSTALL_TYPE}" == "single" ]] || [[ "${1}" == "--force" ]]; then
    echo -e "${YLW}Detected single-user Nix installation.${RST}" >&2
    nix_purge_user_profile
fi
nix_purge_root

echo -e "${GRN}Purged all Nix files from your system.${RST}" >&2
