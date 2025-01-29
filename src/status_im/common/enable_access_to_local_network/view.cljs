(ns status-im.common.enable-access-to-local-network.view
  (:require
   [quo.core :as quo]
   [react-native.core :as rn]
   [status-im.common.enable-access-to-local-network.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- step [{:keys [number text]} button]
  [rn/view {:style {:flex-direction   :row
                    :align-items      :center
                    :column-gap       8
                    :padding-vertical 4}}
   [quo/step {:in-blur-view? true} number]
   [rn/view {:style {:flex-direction :row
                     :align-items    :center
                     :column-gap     4}}
    [quo/text {:weight :regular
               :size   :paragraph-2}
     text]
    button]])

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  "This implementation must be only used on iOS since we need to request the networking
   permission, on Android we already have the permission granted."
  []
  (let [[permission-granted?
         set-permission-granted] (rn/use-state false)
        verify-permission     (rn/use-callback
                               #(rf/dispatch
                                 [:syncing/preflight-outbound-check set-permission-granted])
                               [])
        verifying-permission? nil]
    (prn "permission-granted?:" permission-granted?)
    [:<>
     [quo/drawer-top {:container-style {:padding-bottom 8}
                      :title           (i18n/label :t/enable-access-to-local-network)
                      :type            :default
                      :blur?           true}]
     [quo/text {:style  style/description
                :weight :regular
                :size   :paragraph-1}
      (i18n/label :t/enable-access-to-local-network-description)]
     [rn/view {:style style/list-container}
      [step {:number 1 :text "Open"}
       [quo/button {:type     :primary
                    :size     24
                    :on-press (fn [] (js/alert "a"))}
        "Settings / Privacy / Local Network"]]
      [step {:number 2 :text "Find"}
       [quo/button {:type  :grey
                    :size  24
                    :blur? true}
        "Status"]]
      [step {:number 3 :text "Toggle the switch to grant access"}]
      [step {:number 4 :text "Tap"}
       [quo/button {:type     (if verifying-permission? :grey :primary)
                    :size     24
                    :on-press verify-permission}
        (if verifying-permission?
          "Verifying local network access..."
          "Verify local network access")]]]]))
