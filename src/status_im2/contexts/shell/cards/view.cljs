(ns status-im2.contexts.shell.cards.view
  (:require [clojure.string :as string]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.shell.cards.style :as style]
            [status-im2.contexts.shell.constants :as constants]))

(defn content-container
  [{:keys [content-type data new-notifications? color-50]}]
  [rn/view {:style (style/content-container new-notifications?)}
   ;; TODO - Use status-im2.common.constants for content type
   (case content-type
     :text                           [quo/text style/last-message-text-props data]
     :photo                          [quo/preview-list
                                      {:type               :photo
                                       :more-than-99-label (i18n/label :counter-99-plus)
                                       :size               24
                                       :override-theme     :dark} data]
     :sticker                        [fast-image/fast-image
                                      {:source (:source data)
                                       :style  style/sticker}]
     :gif                            [fast-image/fast-image
                                      {:source (:source data)
                                       :style  style/gif}]
     :channel                        [rn/view
                                      {:style {:flex-direction :row
                                               :align-items    :center}}
                                      [quo/channel-avatar
                                       {:emoji                  (:emoji data)
                                        :emoji-background-color (colors/alpha color-50 0.1)}]
                                      [quo/text style/community-channel-props (:channel-name data)]]
     :community-info                 (case (:type data)
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
                                       (:count :permission) [:<>]) ;; Add components for these cases
     (:audio :community :link :code) ;; Components not available
     [:<>])])

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
  [{:keys [new-notifications?] :as content}]
  [:<>
   [content-container content]
   (when new-notifications?
     [notification-container content])])

(defn avatar
  [avatar-params type customization-color]
  (case type
    constants/one-to-one-chat-card
    [quo/user-avatar
     (merge {:ring?             false
             :size              :medium
             :status-indicator? false}
            avatar-params)]

    constants/private-group-chat-card
    [quo/group-avatar
     {:color          customization-color
      :size           :large
      :override-theme :dark}]

    constants/community-card
    (if (:source avatar-params)
      [fast-image/fast-image
       {:source (:source avatar-params)
        :style  (style/community-avatar customization-color)}]
      ;; TODO - Update to fall back community avatar once designs are available
      [rn/view {:style (style/community-avatar customization-color)}
       [quo/text
        {:weight :semi-bold
         :size   :heading-2
         :style  {:color colors/white-opa-70}}
        (string/upper-case (first (:name avatar-params)))]])))

(defn subtitle
  [{:keys [content-type data]}]
  (case content-type
    :text           (i18n/label :t/message)
    :photo          (i18n/label :t/n-photos {:count (count data)})
    :sticker        (i18n/label :t/sticker)
    :gif            (i18n/label :t/gif)
    :audio          (i18n/label :t/audio-message)
    :community      (i18n/label :t/link-to-community)
    :link           (i18n/label :t/external-link)
    :code           (i18n/label :t/code-snippet)
    :channel        (i18n/label :t/community-channel)
    :community-info (i18n/label :t/community)
    (i18n/label :t/community)))

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
       [quo/text style/title-props title]
       [quo/text style/subtitle-props (subtitle content)]
       [bottom-container (merge {:color-50 color-50 :color-60 color-60} content)]]
      (when avatar-params
        [rn/view {:style style/avatar-container}
         [avatar avatar-params type customization-color]])
      [quo/button (style/close-button-props on-close) :i/close]]]))

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

;; Home Card
(defn communities-discover
  [_]
  [:<>])

(defn card
  [{:keys [type] :as data}]
  (case type
    constants/one-to-one-chat-card    ;; Screens Card
    [screens-card data]

    constants/private-group-chat-card ;; Screens Card
    [screens-card data]

    constants/community-card          ;; Screens Card
    [screens-card data]

    constants/browser-card            ;; Browser Card
    [browser-card data]

    constants/wallet-card             ;; Wallet Card
    [wallet-card data]

    constants/wallet-collectible      ;; Wallet Card
    [wallet-collectible data]

    constants/wallet-graph            ;; Wallet Card
    [wallet-graph data]

    constants/communities-discover    ;; Home Card
    [communities-discover data]))
