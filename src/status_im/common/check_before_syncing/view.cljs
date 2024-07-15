(ns status-im.common.check-before-syncing.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.check-before-syncing.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def checks
  [(i18n/label :t/check-before-syncing-doc-checkbox-1)
   (i18n/label :t/check-before-syncing-doc-checkbox-2)
   (i18n/label :t/check-before-syncing-doc-checkbox-3)])

(defn- checkbox
  [on-change customization-color description]
  [rn/view {:style style/checkbox}
   [quo/selectors
    {:type                :checkbox
     :blur?               true
     :customization-color customization-color
     :on-change           on-change}]
   [quo/text {:style style/checkbox-text} description]])

(defn- compute-new-checked-count
  [set-checked-count checked?]
  (if checked?
    (set-checked-count inc)
    (set-checked-count dec)))

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  [{:keys [on-submit customization-color]}]
  (let [[checked-count set-checked-count] (rn/use-state 0)
        on-change                         (partial compute-new-checked-count set-checked-count)
        render-checkbox                   (rn/use-callback
                                           (partial checkbox on-change customization-color)
                                           [customization-color])
        on-cancel                         hide-bottom-sheet
        on-continue                       (rn/use-callback
                                           (fn []
                                             (hide-bottom-sheet)
                                             (on-submit))
                                           [on-submit])]
    [rn/view
     {:style               style/view-container
      :accessibility-label :check-before-syncing-bottom-sheet}
     [quo/text
      {:weight :semi-bold
       :size   :heading-2
       :style  style/header} (i18n/label :t/check-before-syncing)]
     [quo/text {:style style/description} (i18n/label :t/check-before-syncing-doc-description)]

     (into
      [rn/view {:style style/checkboxes-container}]
      (mapv render-checkbox checks))

     [rn/view {:style style/buttons-container}
      [quo/button
       {:type                :grey
        :container-style     style/button
        :accessibility-label :cancel-button
        :on-press            on-cancel}
       (i18n/label :t/cancel)]
      [quo/button
       {:container-style     style/button
        :disabled?           (not= checked-count (count checks))
        :customization-color (or customization-color :blue)
        :accessibility-label :continue-button

        :on-press            on-continue}
       (i18n/label :t/continue)]]]))
