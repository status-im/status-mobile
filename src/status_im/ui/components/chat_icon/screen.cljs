(ns status-im.ui.components.chat-icon.screen
  (:require [clojure.string :as string]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [quo2.components.avatars.user-avatar.style :as user-avatar.style]
            [quo2.core :as quo]
            [quo2.theme :as theme]
            [re-frame.core :as re-frame.core]
            [status-im.ethereum.ens :as ens]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.styles :as styles]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]))

;;TODO REWORK THIS NAMESPACE

(def get-name-first-char
  (memoize
   (fn [name]
     ;; TODO: for now we check if the first letter is a #
     ;; which means it is most likely a public chat and
     ;; use the second letter if that is the case
     ;; a broader refactoring should clean up upstream params
     ;; for default-chat-icon
     (string/capitalize (if (and (= "#" (first name))
                                 (< 1 (count name)))
                          (second name)
                          (first name))))))

(defn default-chat-icon
  [name styles]
  (when-not (string/blank? name)
    [rn/view (:default-chat-icon styles)
     [rn/text {:style (:default-chat-icon-text styles)}
      (get-name-first-char name)]]))

(defn chat-icon-view
  [chat-id group-chat name styles]
  [rn/view (:container styles)
   (if group-chat
     [default-chat-icon name styles]
     (let [photo-path @(re-frame.core/subscribe [:chats/photo-path chat-id])]
       [photos/photo photo-path styles]))])

(defn emoji-chat-icon
  [emoji styles]
  (when-not (string/blank? emoji)
    [rn/view (:default-chat-icon styles)
     [rn/text {:style (:default-chat-icon-text styles)} emoji]]))

(defn profile-photo-plus-dot-view
  [{:keys [public-key full-name customization-color photo-container photo-path community?]}]
  (let [photo-container         (if (nil? photo-container)
                                  styles/container-chat-list
                                  photo-container)
        size                    (:width photo-container)
        dot-styles              (visibility-status-utils/icon-visibility-status-dot
                                 public-key
                                 size)
        dot-accessibility-label (:accessibility-label dot-styles)
        text-style              (styles/default-chat-icon-text size)]
    [rn/view
     {:style               photo-container
      :accessibility-label :profile-photo}
     (if (:fn photo-path)
       ;; temp support new media server avatar for old component
       [photos/photo
        {:uri ((:fn photo-path)
               {:size             size
                :full-name        full-name
                :font-size        (get text-style :font-size)
                :background-color (user-avatar.style/customization-color customization-color
                                                                         (theme/get-theme))
                :indicator-size   0
                :indicator-border 0
                :indicator-color  "#000000"
                :color            (get text-style :color)
                :length           2
                :ring?            (not (ens/is-valid-eth-name? full-name))
                :ring-width       2})}
        {:size size}]
       [photos/photo photo-path {:size size}])
     (when-not community?
       [rn/view
        {:style               dot-styles
         :accessibility-label dot-accessibility-label}])]))

(defn emoji-chat-icon-view
  [chat-id group-chat name emoji styles]
  [rn/view (:container styles)
   (if group-chat
     (if (string/blank? emoji)
       [default-chat-icon name styles]
       [emoji-chat-icon emoji styles])
     [profile-photo-plus-dot-view
      {:public-key      chat-id
       :photo-container (:default-chat-icon styles)}])])

(defn chat-icon-view-toolbar
  [chat-id group-chat name color emoji size]
  [emoji-chat-icon-view chat-id group-chat name emoji
   {:container              (styles/container-chat-toolbar size)
    :size                   size
    :chat-icon              (styles/chat-icon-chat-toolbar size)
    :default-chat-icon      (styles/default-chat-icon-chat-toolbar color size)
    :default-chat-icon-text (if (string/blank? emoji)
                              (styles/default-chat-icon-text size)
                              (styles/emoji-chat-icon-text size))}])

(defn chat-icon-view-chat-list
  [chat-id group-chat name color]
  [chat-icon-view chat-id group-chat name
   {:container              styles/container-chat-list
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list color)
    :default-chat-icon-text (styles/default-chat-icon-text 40)}])

(defn chat-icon-view-chat-list-redesign
  [chat-id group-chat name color size]
  [chat-icon-view chat-id group-chat name
   {:container              (if (= size 20)
                              (styles/token-icon-container-chat-list size)
                              (styles/community-icon-container-chat-list size))
    :size                   size
    :chat-icon              (if (= size 20)
                              (styles/community-icon-chat-list-redesign size)
                              (styles/community-icon-chat-list size))
    :default-chat-icon      (styles/default-list-chat-icon-redesign color size)
    :default-chat-icon-text (styles/default-chat-icon-text size)}])

(defn chat-icon-view-chat-sheet
  [chat-id group-chat name color]
  [chat-icon-view chat-id group-chat name
   {:container              styles/container-chat-list
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list color)
    :default-chat-icon-text (styles/default-chat-icon-text 40)}])

(defn emoji-chat-icon-view-chat-sheet
  [chat-id group-chat name color emoji]
  [emoji-chat-icon-view chat-id group-chat name emoji
   {:container              styles/container-chat-list
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list color)
    :default-chat-icon-text (if (string/blank? emoji)
                              (styles/default-chat-icon-text 40)
                              (styles/emoji-chat-icon-text 40))}])

(defn custom-icon-view-list
  [name color & [size]]
  [rn/view (styles/container-list-size (or size 40))
   [default-chat-icon name
    {:default-chat-icon      (styles/default-chat-icon-profile color (or size 40))
     :default-chat-icon-text (styles/default-chat-icon-text (or size 40))}]])

(defn contact-icon-view
  [contact {:keys [container] :as styles}]
  [rn/view container
   [photos/photo (multiaccounts/displayed-photo contact) styles]])

(defn contact-icon-contacts-tab
  [{:keys [primary-name] :as contact}]
  [rn/view styles/container-chat-list
   [quo/user-avatar
    {:full-name         primary-name
     :profile-picture   (multiaccounts/displayed-photo contact)
     :size              :small
     :status-indicator? false}]])

(defn dapp-icon-permission
  [contact size]
  [contact-icon-view contact
   {:container              {:width size :height size}
    :size                   size
    :chat-icon              (styles/custom-size-icon size)
    :default-chat-icon      (styles/default-chat-icon-profile colors/default-chat-color size)
    :default-chat-icon-text (styles/default-chat-icon-text size)}])

(defn chat-intro-icon-view
  [icon-text chat-id group-chat styles]
  (if group-chat
    [default-chat-icon icon-text styles]
    (let [photo-path @(re-frame.core/subscribe [:chats/photo-path chat-id])]
      (when-not (string/blank? photo-path)
        [photos/photo photo-path styles]))))

(defn emoji-chat-intro-icon-view
  [icon-text chat-id group-chat emoji styles]
  (if group-chat
    (if (string/blank? emoji)
      [default-chat-icon icon-text styles]
      [emoji-chat-icon emoji styles])
    (let [photo-path @(re-frame.core/subscribe [:chats/photo-path chat-id])]
      (when-not (string/blank? photo-path)
        [photos/photo photo-path styles]))))

(defn profile-icon-view
  [photo-path name color emoji edit? size override-styles public-key community?]
  (let [styles     (merge {:container              {:width size :height size}
                           :size                   size
                           :chat-icon              styles/chat-icon-profile
                           :default-chat-icon      (styles/default-chat-icon-profile color size)
                           :default-chat-icon-text (if (string/blank? emoji)
                                                     (styles/default-chat-icon-text size)
                                                     (styles/emoji-chat-icon-text size))}
                          override-styles)
        photo-path (if (:fn photo-path)
                     ;; temp support new media server avatar for old component
                     {:uri ((:fn photo-path)
                            {:size             size
                             :full-name        name
                             :font-size        (get-in styles [:default-chat-icon-text :font-size])
                             :background-color (get-in styles [:default-chat-icon :background-color])
                             :indicator-size   0
                             :indicator-border 0
                             :indicator-color  "#000000"
                             :color            (get-in styles [:default-chat-icon-text :color])
                             :length           2
                             :ring?            (not (ens/is-valid-eth-name? name))
                             :ring-width       2})}
                     photo-path)]
    [rn/view (:container styles)
     (if (and photo-path (seq photo-path))
       [profile-photo-plus-dot-view
        {:photo-path      photo-path
         :public-key      public-key
         :photo-container (:container styles)
         :community?      community?}]
       [rn/view {:accessibility-label :chat-icon}
        (if (string/blank? emoji)
          [default-chat-icon name styles]
          [emoji-chat-icon emoji styles])])
     (when edit?
       [rn/view {:style (styles/chat-icon-profile-edit)}
        [icons/tiny-icon :tiny-icons/tiny-edit {:color colors/white-persist}]])]))
