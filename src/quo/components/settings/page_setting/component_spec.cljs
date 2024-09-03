(ns quo.components.settings.page-setting.component-spec
  (:require
    [quo.core :as quo]
    [test-helpers.component :as h]))

(h/describe
  "renders basic text"
  (h/test "title is visible"
    (h/render [quo/page-setting
               {:setting-text "sample text"}])
    (h/is-truthy (h/get-by-text "sample text"))))
