(ns env.android.main
  (:require [status-im.android.core :as core]
            [re-frame.interop :as interop]
            [reagent.impl.batching :as batching]))

(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))

(core/init)


