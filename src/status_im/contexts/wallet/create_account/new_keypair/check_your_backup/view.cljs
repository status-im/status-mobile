(ns status-im.contexts.wallet.create-account.new-keypair.check-your-backup.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im.contexts.wallet.create-account.new-keypair.check-your-backup.style :as style]))

(defn- view-internal
  []
  [rn/view {:style {:flex 1}}
   [quo/page-nav
    {:icon-name           :i/arrow-left
     :on-press            #(rf/dispatch [:navigate-back])
     :accessibility-label :top-bar}]
   [quo/text-combinations
    {:container-style style/header-container
     :title           (i18n/label :t/check-your-backup)
     :description     (i18n/label :t/confirm-the-position)}]
   [rn/view {:style {:padding-horizontal 20}}
    ]])

(def view (quo.theme/with-theme view-internal))
