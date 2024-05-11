(ns status-im.common.floating-button-page.component-spec
  (:require [quo.core :as quo]
            [status-im.common.floating-button-page.view :as floating-button-page]
            [test-helpers.component :as h]))


(def sub-mocks {:alert-banners/top-margin 0})

(h/describe "floating button page"
  (h/test "renders with a header and standard button"
    (h/setup-subs sub-mocks)
    (h/render [floating-button-page/view
               {:header [quo/page-nav
                         {:type        :title-description
                          :title       "floating button page"
                          :description "press right icon to swap button type"
                          :text-align  :left
                          :background  :blur
                          :icon-name   :i/close}]
                :footer [quo/button {} "continue"]}])
    (h/is-truthy (h/get-by-text "continue"))
    (h/is-truthy (h/get-by-text "floating button page")))

  (h/test "renders with a header and a slide button"
    (h/setup-subs sub-mocks)
    (h/render [floating-button-page/view
               {:header [quo/page-nav
                         {:type        :title-description
                          :title       "floating button page"
                          :description "press right icon to swap button type"
                          :text-align  :left
                          :background  :blur
                          :icon-name   :i/close}]
                :footer [quo/slide-button
                         {:track-text "We gotta slide"
                          :track-icon :face-id}]}])
    (h/is-truthy (h/get-by-text "We gotta slide"))))
