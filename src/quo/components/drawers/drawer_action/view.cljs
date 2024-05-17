(ns quo.components.drawers.drawer-action.view
  (:require
    [quo.components.drawers.drawer-action.schema :as component-schema]
    [quo.components.drawers.drawer-action.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn view-internal
  [{:keys [action icon description state title on-press
           customization-color blur? accessibility-label]
    :or   {customization-color :blue
           blur?               false}}]
  (let [theme                  (quo.theme/use-theme)
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
     (when icon
       [icon/icon icon
        {:accessibility-label :left-icon
         :container-style     (style/left-icon)
         :color               (style/icon-color {:theme theme
                                                 :blur? blur?})}])

     [rn/view {:style (style/text-container)}
      [text/text {:weight :medium}
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
                                                 :blur? blur?})}]

       (= state :selected)
       [icon/icon :i/check
        {:accessibility-label :check-icon
         :color               (style/check-color {:theme               theme
                                                  :blur?               blur?
                                                  :customization-color customization-color})}])]))

(def view (schema/instrument #'view-internal component-schema/?schema))
