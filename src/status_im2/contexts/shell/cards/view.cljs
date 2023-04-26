(ns status-im2.contexts.shell.cards.view
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.cards.style :as style]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.utils.message-resolver :as resolver]))

(defn content-container
  [type
   {:keys                             [content-type data new-notifications? color-50 community-info
                                       community-channel]
    {:keys [text parsed-text source]} :data}]
  [rn/view {:style (style/content-container new-notifications?)}
   (case type
     shell.constants/community-card
     (case (:type community-info)
       :pending             [quo/status-tag
                             {:status         {:type :pending}
                              :label          (i18n/label :t/pending)
                              :size           :small
                              :override-theme :dark}]
       :kicked              [quo/status-tag
                             {:status         {:type :negative}
                              :size           :small
                              :override-theme :dark
                              :label          (i18n/label :t/kicked)}]
       (:count :permission) [:<>] ;; Add components for these cases

       nil)

     shell.constants/community-channel-card
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      [quo/channel-avatar
       {:emoji                  (:emoji community-channel)
        :emoji-background-color (colors/alpha color-50 0.1)}]
      [quo/text
       {:size            :paragraph-2
        :weight          :medium
        :number-of-lines 1
        :ellipsize-mode  :tail
        :style           style/community-channel}
       (:channel-name community-channel)]]

     (case content-type
       constants/content-type-text
       [quo/text
        {:size            :paragraph-2
         :weight          :regular
         :number-of-lines 1
         :ellipsize-mode  :tail
         :style           style/last-message-text}
        (if parsed-text
          (resolver/resolve-message parsed-text)
          text)]

       constants/content-type-image
       [quo/preview-list
        {:type               :photo
         :more-than-99-label (i18n/label :counter-99-plus)
         :size               24
         :override-theme     :dark} data]

       constants/content-type-sticker
       [fast-image/fast-image
        {:source source
         :style  style/sticker}]

       constants/content-type-gif
       [fast-image/fast-image
        {:source source
         :style  style/gif}]

       constants/content-type-audio
       [quo/audio-tag data {:override-theme :dark}]

       constants/content-type-community
       [quo/community-tag
        (:avatar data)
        (:community-name data)
        {:override-theme :dark}]

       (constants/content-type-link) ;; Components not available
       ;; Code snippet content type is not supported yet
       [:<>]

       nil))])

(defn notification-container
  [{:keys [notification-indicator counter-label color-60]}]
  [rn/view {:style style/notification-container}
   (if (= notification-indicator :counter)
     [quo/counter
      {:outline             false
       :override-text-color colors/white
       :override-bg-color   color-60} counter-label]
     [rn/view {:style (style/unread-dot color-60)}])])

(defn bottom-container
  [type {:keys [new-notifications?] :as content}]
  [:<>
   [content-container type content]
   (when new-notifications?
     [notification-container content])])

(defn avatar
  [avatar-params type customization-color]
  (case type
    shell.constants/one-to-one-chat-card
    [quo/user-avatar
     (merge {:size              :medium
             :status-indicator? false}
            avatar-params)]

    shell.constants/private-group-chat-card
    [quo/group-avatar
     {:color          customization-color
      :size           :large
      :override-theme :dark}]

    (shell.constants/community-card
     shell.constants/community-channel-card)
    (cond
      (:source avatar-params)
      [fast-image/fast-image
       {:source (:source avatar-params)
        :style  (style/community-avatar customization-color)}]

      (:name avatar-params)
      ;; TODO - Update to fall back community avatar once designs are available
      [rn/view {:style (style/community-avatar customization-color)}
       [quo/text
        {:weight :semi-bold
         :size   :heading-2
         :style  {:color colors/white-opa-70}}
        (string/upper-case (first (:name avatar-params)))]])

    nil))

(defn subtitle
  [type {:keys [content-type data]}]
  (case type
    shell.constants/community-card
    (i18n/label :t/community)

    shell.constants/community-channel-card
    (i18n/label :t/community-channel)

    (case content-type
      constants/content-type-text
      (i18n/label :t/message)

      constants/content-type-image
      (i18n/label
       (if (= (count data) 1)
         :t/one-photo
         :t/n-photos)
       {:count (count data)})

      constants/content-type-sticker
      (i18n/label :t/sticker)

      constants/content-type-gif
      (i18n/label :t/gif)

      constants/content-type-audio
      (i18n/label :t/audio-message)

      constants/content-type-community
      (i18n/label :t/link-to-community)

      constants/content-type-link
      (i18n/label :t/external-link)

      "")))

;; Screens Card
(defn screens-card
  [{:keys [avatar-params title type customization-color
           on-press on-close content banner]}]
  (let [color-50 (colors/custom-color customization-color 50)
        color-60 (colors/custom-color customization-color 60)]
    [rn/touchable-without-feedback {:on-press on-press}
     [rn/view {:style (style/base-container color-50)}
      (when banner
        [rn/image
         {:source (:source banner)
          :style  {:width 160}}])
      [rn/view {:style style/secondary-container}
       [quo/text
        {:size            :paragraph-1
         :weight          :semi-bold
         :number-of-lines 1
         :ellipsize-mode  :tail
         :style           style/title}
        title]
       [quo/text
        {:size   :paragraph-2
         :weight :medium
         :style  style/subtitle}
        (subtitle type content)]
       [bottom-container type (merge {:color-50 color-50 :color-60 color-60} content)]]
      (when avatar-params
        [rn/view {:style style/avatar-container}
         [avatar avatar-params type customization-color]])
      [quo/button
       {:size           24
        :type           :grey
        :icon           true
        :on-press       on-close
        :override-theme :dark
        :style          style/close-button}
       :i/close]]]))

;; browser Card
(defn browser-card
  [_]
  [:<>])

;; Wallet Cards
(defn wallet-card
  [_]
  [:<>])

(defn wallet-collectible
  [_]
  [:<>])

(defn wallet-graph
  [_]
  [:<>])

(defn empty-card
  []
  [rn/view {:style (style/empty-card)}])

;; Home Card
(defn communities-discover
  [_]
  [:<>])

(defn card
  [{:keys [type] :as data}]
  (case type
    shell.constants/empty-card            ;; Placeholder
    [empty-card]

    (shell.constants/one-to-one-chat-card ;; Screens Card
     shell.constants/private-group-chat-card
     shell.constants/community-card
     shell.constants/community-channel-card)
    [screens-card data]

    shell.constants/browser-card         ;; Browser Card
    [browser-card data]

    shell.constants/wallet-card          ;; Wallet Card
    [wallet-card data]

    shell.constants/wallet-collectible   ;; Wallet Card
    [wallet-collectible data]

    shell.constants/wallet-graph         ;; Wallet Card
    [wallet-graph data]

    shell.constants/communities-discover ;; Home Card
    [communities-discover data]

    nil))
