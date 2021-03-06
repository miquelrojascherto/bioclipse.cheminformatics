package org.openscience.cdk.controller;

import javax.vecmath.Point2d;

import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.IControllerModule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.selection.AbstractSelection;
import org.openscience.cdk.renderer.selection.IChemObjectSelection;
import org.openscience.cdk.renderer.selection.SingleSelection;

/**
 * @cdk.module control
 */
public abstract class ControllerModuleAdapter implements IControllerModule {

	protected IChemModelRelay chemModelRelay;
	protected IChemObjectSelection selection;

	public ControllerModuleAdapter(IChemModelRelay chemModelRelay) {
		this.chemModelRelay = chemModelRelay;
	}

	public double getHighlightDistance() {
	    RendererModel model = chemModelRelay.getRenderer().getRenderer2DModel();
        return model.getHighlightDistance() / model.getScale();
	}

	public static double distanceToAtom(IAtom atom, Point2d p) {
	    if (atom == null) {
	        return Double.MAX_VALUE;
	    } else {
	        return atom.getPoint2d().distance(p);
	    }
	}

	public static double distanceToBond(IBond bond, Point2d p) {
	    if (bond == null) {
            return Double.MAX_VALUE;
        } else {
            return bond.get2DCenter().distance(p);
        }
	}

	public boolean isBondOnlyInHighlightDistance(double dA, double dB, double dH) {
        return dA > dH && dB < dH;
    }

	public boolean isAtomOnlyInHighlightDistance(double dA, double dB, double dH) {
        return dA < dH && dB > dH;
    }

	public boolean noSelection(double dA, double dB, double dH) {
        return (dH == Double.POSITIVE_INFINITY) || (dA > dH && dB > dH);
    }

	public void mouseWheelMovedBackward(int clicks) {
	}

	public void mouseWheelMovedForward(int clicks) {
	}

	public void mouseClickedDouble(Point2d worldCoord) {
	}

	public void mouseClickedDown(Point2d worldCoord) {
	}

	public void mouseClickedUp(Point2d worldCoord) {
	}

	public void mouseClickedDownRight(Point2d worldCoord) {
	}

	public void mouseClickedUpRight(Point2d worldCoord) {
	}

	public void mouseDrag(Point2d worldCoordFrom, Point2d worldCoordTo) {
	}

	public void mouseEnter(Point2d worldCoord) {
	}

	public void mouseExit(Point2d worldCoord) {
	}

	public void mouseMove(Point2d worldCoord) {
	}

	public void setChemModelRelay(IChemModelRelay relay) {
	}

    protected IChemObject getHighlighted( Point2d worldCoord, IChemObject... objects ) {
        IChemObject closest = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for(IChemObject obj:objects) {
            double distance = Double.POSITIVE_INFINITY;
            if(obj instanceof IAtom)
                distance = distanceToAtom( (IAtom) obj, worldCoord );
            else
                if( obj instanceof IBond)
                    distance = distanceToBond( (IBond)obj, worldCoord );
            if(distance < minDistance) {
                closest = obj;
                minDistance = distance;
            }
        }
        if(minDistance <= getHighlightDistance())
            return closest;
        return null;
    }

	/**
	 * Handles selection behavior. When nothing is within the highlight radius,
	 * null is returned and the selection is cleared. If an atom or bond is
	 * selected that is part of the current selection, the returned atom
	 * container contains the selection. If the atom or bond is not part of the
	 * selection, the selection is updated to contain only the atom or bond
	 * and is returned in the atom container.
	 *
	 * @param worldCoord
	 * @return a AtomContainer containing the atoms/bond that should be affected
	 *         by this action. Otherwise <code>null</code>.
	 */
	protected IAtomContainer getSelectedAtomContainer(Point2d worldCoord) {
		RendererModel rModel =
							chemModelRelay.getRenderer().getRenderer2DModel();
		IAtom atom = chemModelRelay.getClosestAtom(worldCoord);
		IBond bond = chemModelRelay.getClosestBond(worldCoord);

		IChemObjectSelection localSelection = rModel.getSelection();
		IChemObject chemObject = getHighlighted(worldCoord, atom, bond);

		if (!localSelection.contains(chemObject)) {
			if (chemObject != null) {
				localSelection = new SingleSelection<IChemObject>(chemObject);
			} else {
				localSelection = AbstractSelection.EMPTY_SELECTION;
			}
		}
		setSelection(localSelection);
		return selection.getConnectedAtomContainer();
	}

    protected void setSelection(IChemObjectSelection selection) {
        this.selection = selection;
        chemModelRelay.getRenderer().getRenderer2DModel().setSelection(selection);
        chemModelRelay.select( null );
        /* FIXME setSelection on IChemModelRelay
         the selection should probably be in the ControllerHub and not in the
         RendererModel */
        chemModelRelay.updateView();
    }

}
