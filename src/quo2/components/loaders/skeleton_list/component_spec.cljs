(ns quo2.components.loaders.skeleton-list.component-spec
  (:require
    [quo2.components.loaders.skeleton-list.view :as skeleton-list]
    [quo2.foundations.colors :as colors]
    [test-helpers.component :as h]))

(defn test-skeleton
  [content animated?]
  (let [rendered           (h/render [skeleton-list/view
                                      {:index         0
                                       :content       content
                                       :color         colors/neutral-10
                                       :parent-height 600
                                       :animated?     animated?}])
        accessibility-text :skeleton-list]
    (h/is-truthy (h/get-by-label-text rendered accessibility-text))))

(h/describe "Skeleton tests"
  (doseq [content   [:messages :notifications :list-items]
          animated? [true false]]
    (let [content-str (name content)]
      (h/test (str "Skeleton :"
                   content-str
                   " is "
                   (if animated? "animated" "static")
                   " based on animated? " animated?)
        (test-skeleton content animated?)))))
