(ns status-im2.contexts.wallet.send.select-asset.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.send.select-asset.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def tabs-data
  [{:id :tab/assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :tab/collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}])

(defn- asset-component
  []
  (fn [token _ _ _]
    (let [on-press                #(js/alert "Not implemented yet")
          total-balance-formatted (.toFixed (:total-balance token) 2)
          balance-fiat-formatted  (.toFixed (:total-balance-fiat token) 2)
          currency-symbol         "$"]
      [quo/token-network
       {:token       (quo.resources/get-token (keyword (string/lower-case (:symbol token))))
        :label       (:name token)
        :token-value (str total-balance-formatted " " (:symbol token))
        :fiat-value  (str currency-symbol balance-fiat-formatted)
        :networks    (:networks token)
        :on-press    on-press}])))

(defn- asset-list
  [account-address search-text]
  (let [filtered-tokens (rf/sub [:wallet/tokens-filtered account-address search-text])]
    [rn/flat-list
     {:data                         filtered-tokens
      :style                        {:flex 1}
      :content-container-style      {:padding-horizontal 8}
      :keyboard-should-persist-taps :handled
      :key-fn                       :id
      :on-scroll-to-index-failed    identity
      :render-fn                    asset-component}]))

(defn- tab-view
  [account search-text selected-tab]
  (case selected-tab
    :tab/assets       [asset-list account search-text]
    :tab/collectibles [quo/empty-state
                       {:title           (i18n/label :t/no-collectibles)
                        :description     (i18n/label :t/no-collectibles-description)
                        :placeholder?    true
                        :container-style (style/empty-container-style (safe-area/get-top))}]))

(defn- search-input
  [search-text]
  (let [on-change-text #(reset! search-text %)]
    (fn []
      [rn/view {:style style/search-input-container}
       [quo/input
        {:small?         true
         :placeholder    (i18n/label :t/search-assets)
         :icon-name      :i/search
         :value          @search-text
         :on-change-text on-change-text}]])))

(defn- f-view-internal
  []
  (let [{:keys [address]} (rf/sub [:wallet/current-viewing-account])
        selected-tab      (reagent/atom (:id (first tabs-data)))
        search-text       (reagent/atom "")
        account-address   (string/lower-case address)
        on-close          #(rf/dispatch [:navigate-back-within-stack
                                         :wallet-select-asset])]
    (fn []
      [rn/safe-area-view {:style {:flex 1}}
       [rn/scroll-view
        {:content-container-style      {:flex 1}
         :keyboard-should-persist-taps :handled
         :scroll-enabled               false}
        [quo/page-nav
         {:icon-name           :i/arrow-left
          :on-press            on-close
          :accessibility-label :top-bar
          :right-side          :account-switcher
          :account-switcher    {:customization-color :purple
                                :on-press            #(js/alert "Not implemented yet")
                                :state               :default
                                :emoji               "🍑"}}]
        [quo/text-combinations
         {:title                     (i18n/label :t/select-asset)
          :container-style           style/title-container
          :title-accessibility-label :title-label}]
        [quo/segmented-control
         {:size            32
          :blur?           false
          :symbol          false
          :default-active  :tab/assets
          :container-style {:margin-horizontal 20
                            :margin-vertical   8}
          :data            tabs-data
          :on-change       #(reset! selected-tab %)}]
        [search-input search-text]
        [tab-view account-address @search-text @selected-tab]]])))

(defn- view-internal
  []
  [:f> f-view-internal])

(def view (quo.theme/with-theme view-internal))
