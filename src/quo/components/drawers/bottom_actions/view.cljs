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
  :button-one-label - string (default nil) - Label for the first button
  :button-two-label - string (default nil) - Label for the second button
  :button-one-props - map with props for button one
  :button-two-props - map with props for button two
  :theme - :light/:dark
  :scroll? - bool (default false) - Whether the iOS Home Indicator should be rendered"
  [props]
  (let [{:keys [actions description button-one-label button-two-label
                button-one-props button-two-props theme scroll? customization-color]}
        (merge default-props props)]
    [:<>
     [rn/view {:style style/buttons-container}
      (when (= actions :2-actions)
        [button/button
         (merge
          {:size                40
           :background          (when scroll? :blur)
           :container-style     style/button-container-2-actions
           :theme               theme
           :accessibility-label :button-two}
          button-two-props)
         button-two-label])
      [button/button
       (merge
        {:size                40
         :container-style     style/button-container
         :background          (when scroll? :blur)
         :theme               theme
         :accessibility-label :button-one
         :customization-color customization-color}
        button-one-props)
       button-one-label]]
     (when description
       [text/text
        {:size  :paragraph-2
         :style (style/description theme scroll?)} description])]))

(def view (quo.theme/with-theme view-internal))
