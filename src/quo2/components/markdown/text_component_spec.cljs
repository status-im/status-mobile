(ns quo2.components.markdown.text-component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.markdown.text :as text]
            [reagent.core :as reagent]))

(defn render-text
  ([options value]
   (rtl/render (reagent/as-element [text/text options value]))))

(js/global.test "text renders with text"
  (fn []
    (render-text {} "hello")
    (-> (js/expect (rtl/screen.getByText "hello"))
        (.toBeTruthy))))
