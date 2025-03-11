(ns legacy.status-im.ui.screens.help-center.views
  (:require
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [quo.core :as quo]
    [re-frame.core :as re-frame]
    [status-im.constants :as const]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def data
  [{:size                :small
    :title               (i18n/label :t/submit-bug)
    :accessibility-label :submit-bug-button
    :on-press            #(re-frame/dispatch [:open-modal :bug-report])
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/status-help)
    :accessibility-label :status-help-button
    :on-press            #(rf/dispatch [:open-url const/status-help-link])
    :chevron             true}])

(defn help-center
  []
  [:<>
   [quo/page-nav
    {:type       :title
     :title      (i18n/label :t/need-help)
     :background :blur
     :icon-name  :i/close
     :on-press   #(rf/dispatch [:navigate-back])}]
   [list/flat-list
    {:data      data
     :key-fn    (fn [_ i] (str i))
     :render-fn list.item/list-item}]])
