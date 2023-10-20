(ns status-im2.contexts.wallet.account.bridge.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.wallet.account.bridge.style :as style]))


(defn view
  []
  (let [top (safe-area/get-top)]
  [rn/view {:style {:flex       1
                    :margin-top top}}
   [quo/page-nav
    {:icon-name           :i/close
     :on-press            #(rf/dispatch [:navigate-back])
     :accessibility-label :top-bar
     :right-side          :account-switcher
     :account-switcher    {:customization-color :purple
                           :on-press            #(js/alert "Not implemented yet")
                           :state               :default
                           :emoji               "🍑"}}]
   [quo/text-combinations
    {:container-style style/header-container
     :title           (i18n/label :t/bridge)}]
   [quo/input
    {
     :container-style {:padding-horizontal 20
                       :padding-vertical 8}
     :icon-name :i/search
     ;:label           (i18n/label :t/eth-or-ens)
     ;:button          {:on-press (fn [] (clipboard/get-string #(reset! input-value %)))
     ;                  :text     (i18n/label :t/paste)}
     :placeholder     (i18n/label :t/search-assets)
     ;:container-style {:margin-right 12
     ;                  :flex         1}
     ;:weight          :monospace
     ;:on-change       #(reset! input-value %)
     ;:default-value   @input-value
     }]
   ]))
