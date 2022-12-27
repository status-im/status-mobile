(ns quo2.components.banners.--tests--.banner-component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.banners.banner.view :as banner]
            [reagent.core :as reagent]))

(defn render-banner
  [opts]
  (rtl/render (reagent/as-element [banner/banner opts])))

(js/global.test "basic render of banner component"
                (fn []
                  (render-banner {:pins-count      "5"
                                  :latest-pin-text "this message"})
                  (-> (js/expect (rtl/screen.getByText "this message"))
                      (.toBeTruthy))
                  (-> (js/expect (rtl/screen.getByText "5"))
                      (.toBeTruthy))))
