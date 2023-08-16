(ns quo2.components.banners.banner.component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.banners.banner.view :as banner]
            [reagent.core :as reagent]))

(defn render-banner
  [opts]
  (rtl/render (reagent/as-element [banner/view opts])))

(js/global.test "basic render of banner component"
  (fn []
    (render-banner {:pins-count      "5"
                    :latest-pin-text "this message"})
    (-> (js/expect (rtl/screen.getByText "this message"))
        (.toBeTruthy))
    (-> (js/expect (rtl/screen.getByText "5"))
        (.toBeTruthy))))

(js/global.test "banner component fires an event when pressed"
  (let [mock-fn (js/jest.fn)]
    (fn []
      (render-banner {:on-press        mock-fn
                      :pins-count      "5"
                      :latest-pin-text "this message"})
      (rtl/fireEvent.press (rtl/screen.getByText "this message"))
      (-> (js/expect mock-fn)
          (.toHaveBeenCalledTimes 1)))))
