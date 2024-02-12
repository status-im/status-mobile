(ns status-im.contexts.wallet.create-account.new-keypair.generating.view
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))
;(native-module/open-accounts callback)

(defn- view-internal
  []
  (let []
    ;(rn/use-effect #)
    [quo/overlay {:type :shell}
     [rn/view {:style {:flex        1
                       :padding-top 100}}
      [quo/page-top {:title (i18n/label :t/generating-keypair)}]
      [rn/view {:style {:flex            1
                        :justify-content :center
                        :align-items     :center}}
       [rn/view {:style {:background-color colors/red
                         :justify-content  :center
                         :align-items      :center
                         :width            100
                         :height           100}}
        [quo/text "Illustration here"]]]]]))

(defn view
  []
  [:f> view-internal])
