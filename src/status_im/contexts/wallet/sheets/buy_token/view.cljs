(ns status-im.contexts.wallet.sheets.buy-token.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.sheets.buy-token.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- crypto-on-ramp-item
  [{:keys [name description fees logo-url site-url recurrent-site-url urls-need-parameters]
    :as   provider}
   _
   _
   {:keys [account tab]}]
  (let [open-url (rn/use-callback
                  (fn []
                    (if urls-need-parameters
                      (rf/dispatch [:wallet.buy-crypto/select-provider
                                    {:account    account
                                     :provider   provider
                                     :recurrent? (= tab :recurrent)}])
                      (rn/open-url (if (= tab :recurrent) recurrent-site-url site-url))))
                  [site-url recurrent-site-url tab])]
    [quo/settings-item
     {:title             name
      :description       :text
      :description-props {:text description}
      :tag               :context
      :tag-props         {:icon    :i/fees
                          :context fees}
      :action            :arrow
      :action-props      {:alignment :flex-start
                          :icon      :i/external}
      :image             :icon-avatar
      :image-props       {:icon logo-url}
      :on-press          open-url}]))

(def ^:private tabs
  [{:id    :one-time
    :label (i18n/label :t/one-time)}
   {:id    :recurrent
    :label (i18n/label :t/recurrent)}])

(def ^:private initial-tab (:id (first tabs)))

(defn view
  [{:keys [title]}]
  (rn/use-mount (fn []
                  (rf/dispatch [:wallet/get-crypto-on-ramps])))
  (let [crypto-on-ramps                 (rf/sub [:wallet/crypto-on-ramps])
        account                         (rf/sub [:wallet/current-viewing-account-or-default])
        has-recurrent?                  (seq (:recurrent crypto-on-ramps))
        [selected-tab set-selected-tab] (rn/use-state initial-tab)
        [min-height set-min-height]     (rn/use-state 0)
        on-layout                       (rn/use-callback
                                         #(set-min-height
                                           (oops/oget % :nativeEvent :layout :height)))]
    [:<>
     [quo/drawer-top {:title (or title (i18n/label :t/ways-to-buy-assets))}]
     (when has-recurrent?
       [:<>
        [quo/segmented-control
         {:size            32
          :container-style style/tabs
          :default-active  initial-tab
          :on-change       set-selected-tab
          :data            tabs}]
        [rn/flat-list
         {:data        (if (= selected-tab :recurrent)
                         (:recurrent crypto-on-ramps)
                         (:one-time crypto-on-ramps))
          :on-layout   on-layout
          :style       (style/list-container min-height)
          :render-data {:tab     selected-tab
                        :account account}
          :render-fn   crypto-on-ramp-item}]])
     (when-not has-recurrent?
       [rn/flat-list
        {:data        (:one-time crypto-on-ramps)
         :on-layout   on-layout
         :style       (style/list-container min-height)
         :render-data {:account account}
         :render-fn   crypto-on-ramp-item}])]))
