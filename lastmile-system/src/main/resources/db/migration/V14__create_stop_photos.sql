CREATE TABLE stop_photos (
                             id UUID PRIMARY KEY,
                             stop_id UUID NOT NULL REFERENCES stops(id),
                             photo_url TEXT NOT NULL,
                             taken_at TIMESTAMP NOT NULL,
                             photo_order INT NOT NULL
);

CREATE INDEX idx_stop_photos_stop_id ON stop_photos(stop_id);