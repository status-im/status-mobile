(ns quo.components.drawers.drawer-action.view
  (:require
    [quo.components.drawers.drawer-action.schema :as component-schema]
    [quo.components.drawers.drawer-action.style :as style]
    [quo.components.icon :as icon]
    [quo.components.inputs.input.view :as input]
    [quo.components.markdown.text :as text]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn view-internal
  [{:keys       [action icon description state title on-press customization-color
                 blur? accessibility-label input-props]
    action-type :type
    :or         {customization-color :blue
                 blur?               false}}]
  (let [theme                  (quo.theme/use-theme)
        action-type            (or action-type :main)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]

    [rn/pressable
     {:on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :style               (style/container {:state               state
                                             :action              action
                                             :customization-color customization-color
                                             :theme               theme
                                             :pressed?            pressed?
                                             :description?        (not-empty description)
                                             :blur?               blur?})
      :accessibility-label accessibility-label}
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      (when icon
        [icon/icon icon
         {:accessibility-label :left-icon
          :container-style     style/left-icon
          :color               (style/icon-color {:theme theme
                                                  :type  action-type
                                                  :blur? blur?})}])

      [rn/view
       {:style style/text-container}
       [text/text
        (style/text {:theme theme
                     :type  action-type
                     :blur? blur?})
        title]

       (when (seq description)
         [text/text
          {:size  :paragraph-2
           :style (style/desc {:theme theme
                               :blur? blur?})}
          description])]

      (cond
        (= action :toggle)
        [selectors/view
         {:theme               theme
          :label-prefix        "toggle"
          :customization-color customization-color
          :type                :toggle
          :checked?            (= state :selected)}]

        (= action :arrow)
        [icon/icon :i/chevron-right
         {:accessibility-label :arrow-icon
          :color               (style/icon-color {:theme theme
                                                  :type  action-type
                                                  :blur? blur?})}]

        (= state :selected)
        [icon/icon :i/check
         {:accessibility-label :check-icon
          :color               (style/check-color {:theme               theme
                                                   :blur?               blur?
                                                   :customization-color customization-color})}])]

     (when (and (= action :input) (= state :selected))
       [input/input
        (assoc input-props
               :blur?               blur?
               :accessibility-label :input)])]))

(def view (schema/instrument #'view-internal component-schema/?schema))
