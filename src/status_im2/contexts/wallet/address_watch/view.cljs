(ns status-im2.contexts.wallet.address-watch.view
  (:require
    [quo2.core :as quo]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [utils.re-frame :as rf]))

(defn view-internal
  [{:keys [theme]}]
  (let [top          (safe-area/get-top)]
  [rn/view  {:style {:flex       1
                     :margin-top top}}
   [quo/page-nav
    {:type       :no-title
     :icon-name  :i/close
     :on-press   #(rf/dispatch [:navigate-back])}]]))

(def view (quo.theme/with-theme view-internal))
