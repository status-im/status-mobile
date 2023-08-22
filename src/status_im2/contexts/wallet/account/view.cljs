(ns status-im2.contexts.wallet.account.view
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.common.tabs.view :as common]
            [status-im2.contexts.wallet.common.temp :as temp]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.contexts.wallet.account.style :as style]))

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}
   {:id :permissions :label (i18n/label :t/permissions) :accessibility-label :permissions}
   {:id :dapps :label (i18n/label :t/dapps) :accessibility-label :dapps}
   {:id :about :label (i18n/label :t/about) :accessibility-label :about}])


(defn view
  []
  (let [top          (safe-area/get-top)
        selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
      [rn/view
       {:style {:flex       1
                :margin-top top}}
       [quo/page-nav
        {:align-mid?            true
         :mid-section           {:type :text-only :main-text ""}
         :left-section          {:type     :grey
                                 :icon     :i/close
                                 :on-press #(rf/dispatch [:navigate-back])}
         :right-section-buttons [{:type     :grey
                                  :label    "[WIP]"
                                  :on-press #(rf/dispatch [:open-modal :how-to-pair])}]}]
       [quo/account-overview temp/account-overview-state]
       [quo/wallet-graph {:time-frame :empty}]
       [quo/wallet-ctas]
       [quo/tabs
        {:style          style/tabs
         :size           32
         :default-active @selected-tab
         :data           tabs-data
         :on-change      #(reset! selected-tab %)
         :scrollable?    true}]
       (case @selected-tab
         :assets       [rn/flat-list
                        {:render-fn               quo/token-value
                         :data                    temp/tokens
                         :content-container-style {:padding-horizontal 8}}]
         :collectibles [common/empty-tab
                        {:title       (i18n/label :t/no-collectibles)
                         :description (i18n/label :t/no-collectibles-description)}]
         :activity     [common/empty-tab
                        {:title       (i18n/label :t/no-activity)
                         :description (i18n/label :t/empty-tab-description)}]
         :permissions  [common/empty-tab
                        {:title       (i18n/label :t/no-permissions)
                         :description (i18n/label :t/empty-tab-description)}]
         :dapps        [common/empty-tab
                        {:title       (i18n/label :t/no-dapps)
                         :description (i18n/label :t/empty-tab-description)}]
         [rn/view {:style style/wip}
          [quo/text "[WIP]"]])])))
