(ns status-im.contexts.shell.jump-to.components.jump-to-screen.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.safe-area :as safe-area]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.common.resources :as resources]
    [status-im.contexts.shell.jump-to.components.bottom-tabs.view :as bottom-tabs]
    [status-im.contexts.shell.jump-to.components.jump-to-screen.style :as style]
    [status-im.contexts.shell.jump-to.components.switcher-cards.view :as switcher-cards]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.contexts.shell.jump-to.state :as state]
    [status-im.contexts.shell.jump-to.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn placeholder
  []
  [rn/view {:style (style/placeholder-container (safe-area/get-top))}
   [linear-gradient/linear-gradient
    {:colors [colors/neutral-100-opa-0 colors/neutral-100-opa-100]
     :start  {:x 0 :y 0}
     :end    {:x 0 :y 1}
     :style  (style/placeholder-container (safe-area/get-top))}]
   [quo/empty-state
    {:title       (i18n/label :t/shell-placeholder-title)
     :description (i18n/label :t/shell-placeholder-subtitle)
     :image       (resources/get-themed-image :jump-to :dark)}]])

(defn jump-to-text
  []
  [quo/text
   {:size   :heading-1
    :weight :semi-bold
    :style  (style/jump-to-text (safe-area/get-top))}
   (i18n/label :t/jump-to)])

(defn render-card
  [{:keys [type screen-id] :as card}]
  (let [card-data (cond
                    (= type shell.constants/one-to-one-chat-card)
                    (rf/sub [:shell/one-to-one-chat-card screen-id])

                    (= type shell.constants/private-group-chat-card)
                    (rf/sub [:shell/private-group-chat-card screen-id])

                    (= type shell.constants/community-card)
                    (rf/sub [:shell/community-card screen-id])

                    (= type shell.constants/community-channel-card)
                    (rf/sub [:shell/community-channel-card screen-id])

                    :else nil)]
    [switcher-cards/card (merge card card-data)]))

(def empty-cards (repeat 6 {:type shell.constants/empty-card}))

(defn jump-to-list
  [switcher-cards]
  (let [data (if (seq switcher-cards) switcher-cards empty-cards)]
    [:<>
     [rn/flat-list
      {:data                              data
       :render-fn                         render-card
       :key-fn                            :screen-id
       :header                            (jump-to-text)
       :ref                               #(reset! state/jump-to-list-ref %)
       :num-columns                       2
       :column-wrapper-style              {:margin-horizontal 20
                                           :justify-content   :space-between
                                           :margin-bottom     16}
       :style                             style/jump-to-list
       :content-inset-adjustment-behavior :never
       :content-container-style           {:padding-bottom (utils/bottom-tabs-container-height)}}]
     (when-not (seq switcher-cards)
       [placeholder])]))

(defn top-nav-blur-overlay
  [top]
  (let [pass-through? (rf/sub [:shell/shell-pass-through?])]
    [rn/view {:style (style/top-nav-blur-overlay-container (+ 56 top) pass-through?)}
     (when pass-through?
       [quo/blur (bottom-tabs/blur-overlay-params style/top-nav-blur-overlay)])]))

(defn view
  []
  (let [switcher-cards (rf/sub [:shell/sorted-switcher-cards])
        top            (safe-area/get-top)]
    [quo.theme/provider :dark
     [rn/view
      {:style {:top              0
               :left             0
               :right            0
               :bottom           -1
               :position         :absolute
               :background-color colors/neutral-100}}
      [jump-to-list switcher-cards]
      [top-nav-blur-overlay top]
      [common.top-nav/view
       {:jump-to?        true
        :container-style {:margin-top top
                          :z-index    2}}]]]))
