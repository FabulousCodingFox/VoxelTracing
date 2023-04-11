package de.fabulousfox.engine.wrapper;

import org.joml.Vector3f;

public record LightSource(Vector3f position, float strength, Vector3f color) {
}
