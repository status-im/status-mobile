(ns quo2.components.loaders.skeleton-list.component-spec
  (:require [quo2.components.loaders.skeleton-list.view :as skeleton-list]
            [quo2.foundations.colors :as colors]
            [test-helpers.component :as h]))

(h/describe "Skeleton tests"
  (doseq [i (range 0 2)]
    (h/test (str "Skeleton :messages component with index " i " is animated when animated? is true")
      (let [rendered (h/render [skeleton-list/view
                                {:index         0
                                 :content       :messages
                                 :color         colors/neutral-10
                                 :parent-height 600
                                 :animated?     true}])]
        (h/is-truthy (h/get-by-label-text rendered (str "skeleton-animated-" (mod i 4) "-" i))))))

  (doseq [i (range 0 2)]
    (h/test (str "Skeleton :messages component with index " i " is static when animated? is false")
      (let [rendered (h/render [skeleton-list/view
                                {:index         0
                                 :content       :messages
                                 :color         colors/neutral-10
                                 :parent-height 600
                                 :animated?     false}])]
        (h/is-truthy (h/get-by-label-text rendered (str "skeleton-static-" (mod i 4) "-" i))))))

  (doseq [i (range 0 2)]
    (h/test (str "Skeleton :notifications component with index " i " is animated when animated? is true")
      (let [rendered (h/render [skeleton-list/view
                                {:index         0
                                 :content       :notifications
                                 :color         colors/neutral-10
                                 :parent-height 600
                                 :animated?     true}])]
        (h/is-truthy (h/get-by-label-text rendered (str "skeleton-animated-" (mod i 4) "-" i))))))

  (doseq [i (range 0 2)]
    (h/test (str "Skeleton :notifications component with index " i " is static when animated? is false")
      (let [rendered (h/render [skeleton-list/view
                                {:index         0
                                 :content       :notifications
                                 :color         colors/neutral-10
                                 :parent-height 600
                                 :animated?     false}])]
        (h/is-truthy (h/get-by-label-text rendered (str "skeleton-static-" (mod i 4) "-" i))))))

  (doseq [i (range 0 2)]
    (h/test (str "Skeleton :list-items component with index " i " is animated when animated? is true")
      (let [rendered (h/render [skeleton-list/view
                                {:index         0
                                 :content       :list-items
                                 :color         colors/neutral-10
                                 :parent-height 600
                                 :animated?     true}])]
        (h/is-truthy (h/get-by-label-text rendered (str "skeleton-animated-" (mod i 4) "-" i))))))

  (doseq [i (range 0 2)]
    (h/test (str "Skeleton :list-items component with index " i " is static when animated? is false")
      (let [rendered (h/render [skeleton-list/view
                                {:index         0
                                 :content       :list-items
                                 :color         colors/neutral-10
                                 :parent-height 600
                                 :animated?     false}])]
        (h/is-truthy (h/get-by-label-text rendered (str "skeleton-static-" (mod i 4) "-" i)))))))
