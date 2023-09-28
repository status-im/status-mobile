(ns status-im2.contexts.wallet.send.select-address.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.wallet.send.select-address.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def tabs-data
  [{:id :tab/recent :label (i18n/label :t/recent) :accessibility-label :recent-tab}
   {:id :tab/saved :label (i18n/label :t/saved) :accessibility-label :saved-tab}
   {:id :tab/contacts :label (i18n/label :t/contacts) :accessibility-label :contacts-tab}
   {:id :tab/my-accounts :label (i18n/label :t/my-accounts) :accessibility-label :my-accounts-tab}])

(defn- tab-view
  [selected-tab]
  (case selected-tab
    :tab/recent      [quo/empty-state
                      {:title           (i18n/label :t/no-recent-transactions)
                       :description     (i18n/label :t/make-one-it-is-easy-we-promise)
                       :placeholder?    true
                       :container-style style/empty-container-style}]
    :tab/saved       [quo/empty-state
                      {:title           (i18n/label :t/no-saved-addresses)
                       :description     (i18n/label :t/you-like-to-type-43-characters)
                       :placeholder?    true
                       :container-style style/empty-container-style}]
    :tab/contacts    [quo/empty-state
                      {:title           (i18n/label :t/no-contacts)
                       :description     (i18n/label :t/no-contacts-description)
                       :placeholder?    true
                       :container-style style/empty-container-style}]
    :tab/my-accounts [quo/empty-state
                      {:title           (i18n/label :t/no-other-accounts)
                       :description     (i18n/label :t/here-is-a-cat-in-a-box-instead)
                       :placeholder?    true
                       :container-style style/empty-container-style}]))

(defn- address-input
  []
  (let [timer                    (atom nil)
        valid-ens-or-address?    (reagent/atom false)
        input-value              (atom "")
        on-detect-address-or-ens (fn [_]
                                   (reset! valid-ens-or-address? false)
                                   (when @timer (js/clearTimeout @timer))
                                   (reset! timer (js/setTimeout #(reset! valid-ens-or-address? true)
                                                                2000)))]
    (fn []
      (let [scanned-address (rf/sub [:wallet-2/scanned-address])]
        [quo/address-input
         {:on-scan               #(rf/dispatch [:open-modal :scan-address])
          :ens-regex             constants/regx-ens
          :address-regex         constants/regx-address
          :scanned-value         scanned-address
          :on-detect-ens         on-detect-address-or-ens
          :on-detect-address     on-detect-address-or-ens
          :on-change-text        (fn [text]
                                   (when-not (= scanned-address text)
                                     (rf/dispatch [:wallet-2/clean-scanned-address]))
                                   (reset! input-value text))
          :on-clear              #(rf/dispatch [:wallet-2/clean-scanned-address])
          :valid-ens-or-address? @valid-ens-or-address?}]))))

(defn- f-view-internal
  []
  (let [margin-top    (safe-area/get-top)
        selected-tab  (reagent/atom (:id (first tabs-data)))
        on-close      #(rf/dispatch [:dismiss-modal :wallet-select-address])
        on-change-tab #(reset! selected-tab %)]
    (fn []
      (rn/use-effect (fn [] #(rf/dispatch [:wallet-2/clean-scanned-address])))
      [rn/scroll-view
       {:content-container-style      (style/container margin-top)
        :keyboard-should-persist-taps :never
        :scroll-enabled               false}
       [quo/page-nav
        {:icon-name           :i/close
         :on-press            on-close
         :accessibility-label :top-bar
         :right-side          :account-switcher
         :account-switcher    {:customization-color :purple
                               :on-press            #(js/alert "Not implemented yet")
                               :state               :default
                               :emoji               "🍑"}}]
       [quo/text-combinations
        {:title                     (i18n/label :t/send-to)
         :container-style           style/title-container
         :title-accessibility-label :title-label}]
       [address-input]
       [quo/divider-line]
       [quo/tabs
        {:style            style/tabs
         :container-style  style/tabs-content
         :size             32
         :default-active   @selected-tab
         :data             tabs-data
         :scrollable?      true
         :scroll-on-press? true
         :on-change        on-change-tab}]
       [tab-view @selected-tab]])))

(defn view-internal
  []
  [:f> f-view-internal])

(def view (quo.theme/with-theme view-internal))
