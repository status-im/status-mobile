(ns status-im2.contexts.shell.jump-to.components.switcher-cards.view
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]
            [quo2.core :as quo]
            [utils.re-frame :as rf]
            [status-im2.config :as config]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.jump-to.animation :as animation]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]
            [status-im2.contexts.shell.jump-to.components.switcher-cards.style :as style]
            [status-im2.contexts.chat.messages.resolver.message-resolver :as resolver]))

(defn- channel-card
  [{:keys [emoji channel-name customization-color]}]
  [rn/view style/channel-card-container
   [quo/channel-avatar
    {:emoji               emoji
     :full-name           channel-name
     :customization-color customization-color}]
   [rn/view style/channel-card-text-container
    [quo/text
     {:size            :paragraph-2
      :weight          :medium
      :number-of-lines 1
      :ellipsize-mode  :tail
      :style           style/community-channel}
     channel-name]]])

(defn content-container
  [type
   {:keys                             [content-type data new-notifications? color-50
                                       community-info community-channel]
    {:keys [text parsed-text source]} :data}]
  [rn/view {:style (style/content-container new-notifications?)}
   (case type
     shell.constants/community-card
     (case (:type community-info)
       :pending             [quo/status-tag
                             {:status {:type :pending}
                              :label  (i18n/label :t/pending)
                              :size   :small}]
       :kicked              [quo/status-tag
                             {:status {:type :negative}
                              :size   :small
                              :label  (i18n/label :t/kicked)}]
       (:count :permission) [:<>] ;; Add components for these cases
       nil)

     shell.constants/community-channel-card
     [channel-card (assoc community-channel :customization-color color-50)]

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
         :size               24}
        data]

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
  [{:keys [notification-indicator counter-label customization-color]}]
  [rn/view {:style style/notification-container}
   (if (= notification-indicator :counter)
     [quo/counter
      {:customization-color customization-color}
      counter-label]
     [rn/view {:style (style/unread-dot customization-color)}])])

(defn bottom-container
  [type {:keys [new-notifications?] :as content}]
  [rn/view {:style style/bottom-container}
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
     {:customization-color customization-color
      :size                :size/s-48
      :override-theme      :dark}]

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

      constants/content-type-contact-request
      (i18n/label :t/contact-request)

      "")))

(defn open-screen
  [card-type id channel-id]
  (cond
    (#{shell.constants/one-to-one-chat-card
       shell.constants/private-group-chat-card}
     card-type)
    (rf/dispatch [:chat/navigate-to-chat id])

    (= card-type shell.constants/community-channel-card)
    (if config/shell-navigation-disabled?
      (do
        (rf/dispatch [:navigate-to :community-overview id])
        (js/setTimeout
         #(rf/dispatch [:chat/navigate-to-chat channel-id])
         100))
      (rf/dispatch [:chat/navigate-to-chat channel-id]))

    (= card-type shell.constants/community-card)
    (rf/dispatch [:navigate-to :community-overview id])))

(defn calculate-card-position-and-open-screen
  [card-ref card-type id channel-id]
  (when @card-ref
    (.measure
     ^js
     @card-ref
     (fn [_ _ _ _ page-x page-y]
       (animation/set-floating-screen-position
        page-x
        page-y
        card-type)
       (open-screen card-type id channel-id)))))

;; Screens Card
(defn screens-card
  []
  (let [card-ref (atom nil)]
    (fn [{:keys [avatar-params title type customization-color
                 content banner id channel-id]}]
      (let [color-50 (colors/custom-color customization-color 50)]
        [rn/touchable-opacity
         {:on-press       #(calculate-card-position-and-open-screen
                            card-ref
                            type
                            id
                            channel-id)
          :ref            #(reset! card-ref %)
          :active-opacity 1}
         [rn/view {:style (style/base-container color-50)}
          (when banner
            [rn/image
             {:source banner
              :style  {:width  160
                       :height 65}}])
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
           [bottom-container type
            (merge {:color-50            color-50
                    :customization-color customization-color}
                   content)]]
          (when avatar-params
            [rn/view {:style style/avatar-container}
             [avatar avatar-params type customization-color]])
          [quo/button
           {:size            24
            :type            :grey
            :icon-only?      true
            :on-press        #(rf/dispatch [:shell/close-switcher-card id])
            :container-style style/close-button}
           :i/close]]]))))

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
