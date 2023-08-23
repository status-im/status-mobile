(ns status-im2.contexts.wallet.home.view
  (:require
    [react-native.core :as rn]
    [quo2.core :as quo]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.home.view :as common.home]
    [status-im2.contexts.wallet.home.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.wallet.home.temp :as temp]))

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}])

(defn collectibles
  []
  [rn/view {:style style/empty-container}
   [rn/view {:style style/image-placeholder}]
   [quo/text {:weight :semi-bold :style {:margin-top 12}} (i18n/label :t/no-collectibles)]
   [quo/text {:size :paragraph-2 :style {:margin-top 2}} (i18n/label :t/no-collectibles-description)]])

(defn activity
  []
  [rn/view {:style style/empty-container}
   [rn/view {:style style/image-placeholder}]
   [quo/text {:weight :semi-bold :style {:margin-top 12}} (i18n/label :t/no-activity)]
   [quo/text {:size :paragraph-2 :style {:margin-top 2}} (i18n/label :t/no-activity-description)]])

(defn header
  [selected-tab]
  [rn/view
   [rn/pressable
    {:on-long-press #(rf/dispatch [:show-bottom-sheet {:content temp/wallet-temporary-navigation}])}
    [quo/wallet-graph temp/wallet-graph-state]]
   [rn/flat-list
    {:style      style/accounts-list
     :data       temp/account-cards
     :horizontal true
     :separator  [rn/view {:style {:width 12}}]
     :render-fn  quo/account-card}]
   [quo/tabs
    {:style          style/tabs
     :size           32
     :default-active @selected-tab
     :data           tabs-data
     :on-change      #(reset! selected-tab %)}]])

(defn view
  []
  (let [top          (safe-area/get-top)
        selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
      [rn/view
       {:style {:margin-top top
                :flex       1}}
       [rn/view {:style {:position :absolute
                         :left 0
                         :right 0
                         :z-index 2
                         :border-bottom-width 1
                         :background-color :red}}
        [common.home/top-nav {:type :grey}]
        [quo/wallet-overview temp/wallet-overview-state]]

       (case @selected-tab
         :assets       [rn/flat-list
                        {:render-fn               quo/token-value
                         :header  [header selected-tab]
                         :data                    temp/tokens
                         :content-container-style {:padding-horizontal 0
                                                   :padding-top (+ 98 44)}}]
         :collectibles [collectibles]
         [activity])])))
