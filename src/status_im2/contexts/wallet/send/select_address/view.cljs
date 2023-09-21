(ns status-im2.contexts.wallet.send.select-address.view
  (:require [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im2.contexts.wallet.send.select-address.style :as style]
            [react-native.safe-area :as safe-area]
            [quo2.core :as quo]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [status-im2.constants :as constants]
            [reagent.core :as reagent]))

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
  (let [timer       (atom nil)
        valid-ens?  (reagent/atom false)
        input-value (atom "")]
    [quo/address-input
     {:on-scan #(js/alert "Not implemented yet")
      :ens-regex constants/regx-ens
      :on-detect-ens
      (fn [_]
        (reset! valid-ens? false)
        (when @timer (js/clearTimeout @timer))
        (reset! timer (js/setTimeout #(reset! valid-ens? true) 2000)))
      :on-change-text #(reset! input-value %)
      :valid-ens? @valid-ens?}]))

(defn- view-internal
  []
  (let [margin-top    (safe-area/get-top)
        selected-tab  (reagent/atom (:id (first tabs-data)))
        on-close      #(rf/dispatch [:navigate-back])
        on-change-tab #(reset! selected-tab %)]
    (fn []
      [rn/scroll-view
       {:content-container-style      (style/container margin-top)
        :keyboard-should-persist-taps :never
        :scroll-enabled               false}
       [quo/page-nav
        {:icon-name           :i/close
         :on-press            on-close
         :accessibility-label :top-bar
         :right-side          :account-switcher}]
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

(def view (quo.theme/with-theme view-internal))
