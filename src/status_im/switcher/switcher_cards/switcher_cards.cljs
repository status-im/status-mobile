(ns status-im.switcher.switcher-cards.switcher-cards
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.buttons.button :as button]
            [quo2.components.counter.counter :as counter]
            [quo2.components.tags.status-tags :as status-tags]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.components.avatars.group-avatar :as group-avatar]
            [quo2.components.list-items.preview-list :as preview-list]
            [quo2.components.avatars.channel-avatar :as channel-avatar]
            [status-im.switcher.switcher-cards.styles :as styles]
            [status-im.i18n.i18n :as i18n]))

(defn content-container [{:keys [content-type data new-notifications? color-50]}]
  [rn/view {:style (styles/content-container new-notifications?)}
   (case content-type
     :text [text/text (styles/last-message-text-props) data]
     :photo [preview-list/preview-list {:type               :photo
                                        :more-than-99-label (i18n/label :counter-99-plus)
                                        :size               24
                                        :override-theme     :dark} data]
     :sticker [fast-image/fast-image {:source (:source data)
                                      :style  (styles/sticker)}]
     :gif [fast-image/fast-image {:source (:source data)
                                  :style  (styles/gif)}]
     :channel [rn/view {:style {:flex-direction :row
                                :align-items    :center}}
               [channel-avatar/channel-avatar
                {:emoji                  (:emoji data)
                 :emoji-background-color (colors/alpha color-50 0.1)}]
               [text/text (styles/community-channel-props) (:channel-name data)]]
     :community-info (case (:type data)
                       :pending      [status-tags/status-tag
                                      {:status         :pending
                                       :label          (i18n/label :t/pending)
                                       :size           :small
                                       :override-theme :dark}]
                       :kicked      [status-tags/status-tag
                                     {:status         :negative
                                      :size           :small
                                      :override-theme :dark
                                      :label          (i18n/label :t/kicked)}]
                       (:count :permission) [:<>]) ;; Add components for these cases
     (:audio :community :link :code) ;; Components not available
     [:<>])])

(defn notification-container [{:keys [notification-indicator counter-label color-60]}]
  [rn/view {:style (styles/notification-container)}
   (if (= notification-indicator :counter)
     [counter/counter {:outline             false
                       :override-text-color colors/white
                       :override-bg-color   color-60} counter-label]
     [rn/view {:style (styles/unread-dot color-60)}])])

(defn bottom-container [{:keys [new-notifications?] :as content}]
  [:<>
   [content-container content]
   (when new-notifications?
     [notification-container content])])

(defn avatar [avatar-params type customization-color]
  (case type
    :messaging       [user-avatar/user-avatar
                      (merge {:ring?             false
                              :size              :medium
                              :status-indicator? false}
                             avatar-params)]
    :group-messaging [group-avatar/group-avatar {:color          customization-color
                                                 :size           :large
                                                 :override-theme :dark}]
    :community-card  [fast-image/fast-image {:source (:source avatar-params)
                                             :style  (styles/community-avatar)}]))

(defn subtitle [{:keys [content-type data]}]
  (case content-type
    :text (i18n/label :t/message)
    :photo (i18n/label :t/n-photos {:count (count data)})
    :sticker (i18n/label :t/sticker)
    :gif (i18n/label :t/gif)
    :audio (i18n/label :t/audio-message)
    :community (i18n/label :t/link-to-community)
    :link (i18n/label :t/external-link)
    :code (i18n/label :t/code-snippet)
    :channel (i18n/label :t/community-channel)
    :community-info (i18n/label :t/community)))

;; Screens Card
(defn screens-card [{:keys [avatar-params title type customization-color
                            on-press on-close content banner]}]
  (let [color-50 (colors/custom-color customization-color 50)
        color-60 (colors/custom-color customization-color 60)]
    [rn/touchable-without-feedback {:on-press on-press}
     [rn/view {:style (styles/base-container color-50)}
      (when banner
        [rn/image {:source (:source banner)
                   :style  {:width  160}}])
      [rn/view {:style (styles/secondary-container)}
       [text/text (styles/title-props) title]
       [text/text (styles/subtitle-props) (subtitle content)]
       [bottom-container (merge {:color-50 color-50 :color-60 color-60} content)]]
      (when avatar
        [rn/view {:style (styles/avatar-container)}
         [avatar avatar-params type customization-color]])
      [button/button (styles/close-button-props on-close) :i/close]]]))

;; browser Card
(defn browser-card [_]
  [:<>])

;; Wallet Cards
(defn wallet-card [_]
  [:<>])

(defn wallet-collectible [_]
  [:<>])

(defn wallet-graph [_]
  [:<>])

;; Home Card
(defn communities-discover [_]
  [:<>])

(defn card [type data]
  (case type
    :communities-discover [communities-discover data] ;; Home Card
    :messaging            [screens-card data]         ;; Screens Card
    :group-messaging      [screens-card data]         ;; Screens Card
    :community-card       [screens-card data]         ;; Screens Card
    :browser-card         [browser-card data]         ;; Browser Card
    :wallet-card          [wallet-card data]          ;; Wallet Card
    :wallet-collectible   [wallet-collectible data]   ;; Wallet Card
    :wallet-graph         [wallet-graph data]))       ;; Wallet Card
