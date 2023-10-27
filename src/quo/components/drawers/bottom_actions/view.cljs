(ns quo.components.drawers.bottom-actions.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.drawers.bottom-actions.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(def default-props
  {:button-one-type     :primary
   :button-two-type     :grey
   :customization-color :blue})

(defn- view-internal
  "Options:

       :actions - keyword (default nil) - :1-action/:2-actions
       :description - string (default nil) - Description to display below the title
       :button-one-disabled? - bool (default false) - Whether the first button is
       disabled
       :button-two-disabled? - bool (default false) - Whether the second button is
       disabled
       :button-one-label - string (default nil) - Label for the first button
       :button-two-label - string (default nil) - Label for the second button
       :button-one-press - fn (default nil) - Function to call when the first button is pressed
       :button-two-press - fn (default nil) - Function to call when the second button is pressed
       :theme - :light/:dark
       :scroll? - bool (default false) - Whether the iOS Home Indicator should be
       rendered
       :customization-color - keyword (default nil) - Color for the first button
       :button-one-type - same as button/button :type
       :button-two-type - same as button/button :type"
  [props]
  (let [{:keys [actions description button-one-disabled?
                button-two-disabled? button-one-label button-two-label
                button-one-press button-two-press theme
                scroll? customization-color button-one-type button-two-type]}
        (merge default-props props)]
    [:<>
     [rn/view {:style style/buttons-container}
      (when (= actions :2-actions)
        [button/button
         {:size            40
          :disabled?       button-two-disabled?
          :background      (when scroll? :blur)
          :container-style style/button-container-2-actions
          :type            button-two-type
          :on-press        button-two-press} button-two-label])
      [button/button
       {:size                40
        :disabled?           button-one-disabled?
        :container-style     style/button-container
        :type                button-one-type
        :background          (when scroll? :blur)
        :on-press            button-one-press
        :customization-color customization-color} button-one-label]]
     (when description
       [text/text
        {:size  :paragraph-2
         :style (style/description theme scroll?)} description])]))

(def view (quo.theme/with-theme view-internal))
