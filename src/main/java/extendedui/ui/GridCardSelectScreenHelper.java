package extendedui.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import eatyourbeets.interfaces.delegates.*;
import extendedui.ui.controls.GUI_TextBox;
import extendedui.ui.hitboxes.AdvancedHitbox;
import extendedui.utilities.ClassUtils;
import extendedui.utilities.GenericCondition;

import java.util.ArrayList;

// Copied and modified from https://github.com/EatYourBeetS/STS-AnimatorMod and https://github.com/SevenDayCandle/STS-FoolMod

public class GridCardSelectScreenHelper
{
    private static final CardGroup mergedGroup = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
    private static final ArrayList<CardGroup> cardGroups = new ArrayList<>();
    private static final GUI_TextBox dynamicLabel = new GUI_TextBox(ImageMaster.WHITE_SQUARE_IMG,
            new AdvancedHitbox(Settings.WIDTH / 4.0F, 96.0F * Settings.scale, Settings.WIDTH / 2.0F, 48.0F * Settings.scale))
            .SetColors(new Color(0, 0, 0, 0.85f), Settings.CREAM_COLOR)
            .SetAlignment(0.7f, 0.15f, true, false)
            .SetFont(FontHelper.cardDescFont_N, 1f);
    private static GenericCondition<ArrayList<AbstractCard>> condition;
    private static FuncT1<String, ArrayList<AbstractCard>> dynamicString;
    private static ActionT3<CardGroup, ArrayList<AbstractCard>, AbstractCard> onClickCard;
    private static boolean enabled = false;

    public static void Clear(boolean clearFunctions)
    {
        cardGroups.clear();
        mergedGroup.clear();
        if (clearFunctions) {
            condition = null;
            dynamicString = null;
            onClickCard = null;
        }
    }

    public static void SetCondition(GenericCondition<ArrayList<AbstractCard>> newCondition) {
        condition = newCondition;
    }

    public static void SetDynamicLabel(FuncT1<String, ArrayList<AbstractCard>> stringFunc) {
        dynamicString = stringFunc;
        if (dynamicString != null) {
            dynamicLabel
                    .SetText(dynamicString.Invoke(AbstractDungeon.gridSelectScreen.selectedCards))
                    .Autosize(1f, null)
                    .SetPosition((Settings.WIDTH / 2.0F) - dynamicLabel.hb.width / 8, 96.0F * Settings.scale);
        }
    }

    public static void SetOnClickCard(ActionT3<CardGroup, ArrayList<AbstractCard>, AbstractCard> newOnClickCard) {
        onClickCard = newOnClickCard;
    }

    public static void AddGroup(CardGroup cardGroup)
    {
        if (!cardGroup.isEmpty())
        {
            cardGroups.add(cardGroup);
            mergedGroup.group.addAll(cardGroup.group);
        }

        enabled = !mergedGroup.isEmpty();
    }

    public static CardGroup GetCardGroup()
    {
        return mergedGroup;
    }

    public static void Open(GridCardSelectScreen selectScreen)
    {
        if (!enabled)
        {
            Clear(false);
        }
        else
        {
            if (cardGroups.size() == 1)
            {
                ClassUtils.SetField(selectScreen, "targetGroup", cardGroups.get(0));
                Clear(false);
            }

            enabled = false;
        }
    }

    public static boolean IsConditionMet() {
        return condition == null || condition.Check(AbstractDungeon.gridSelectScreen.selectedCards);
    }

    public static boolean UpdateCardPositionAndHover(GridCardSelectScreen selectScreen)
    {
        if (cardGroups.isEmpty())
        {
            return false;
        }

        float lineNum = 0;

        float drawStartX = ClassUtils.GetField(selectScreen, "drawStartX");
        float drawStartY = ClassUtils.GetField(selectScreen, "drawStartY");
        float padX = ClassUtils.GetField(selectScreen, "padX");
        float padY = ClassUtils.GetField(selectScreen, "padY");
        float currentDiffY = ClassUtils.GetField(selectScreen, "currentDiffY");

        ClassUtils.SetField(selectScreen, "hoveredCard", null);

        for (CardGroup cardGroup : cardGroups)
        {
            ArrayList<AbstractCard> cards = cardGroup.group;
            for (int i = 0; i < cards.size(); ++i)
            {
                int mod = i % 5;
                if (mod == 0 && i != 0)
                {
                    lineNum += 1;
                }

                AbstractCard card = cards.get(i);

                card.target_x = drawStartX + (float) mod * padX;
                card.target_y = drawStartY + currentDiffY - lineNum * padY;
                card.fadingOut = false;
                card.stopGlowing();
                card.update();
                card.updateHoverLogic();

                if (card.hb.hovered)
                {
                    ClassUtils.SetField(selectScreen, "hoveredCard", card);
                }
            }

            lineNum += 1.3f;
        }

        return true;
    }

    public static boolean CalculateScrollBounds(GridCardSelectScreen instance)
    {
        if (cardGroups.isEmpty())
        {
            return false;
        }

        float padY = ClassUtils.GetField(instance, "padY");
        CardGroup targetCardGroup = ClassUtils.GetField(instance, "targetGroup");

        float scrollTmp = (mergedGroup.size() + 2.6f * cardGroups.size()) / 5f - 2;
        if (targetCardGroup.size() % 5 != 0)
        {
            scrollTmp += 1;
        }

        ClassUtils.SetField(instance, "scrollUpperBound", Settings.DEFAULT_SCROLL_LIMIT + scrollTmp * padY);
        ClassUtils.SetField(instance, "prevDeckSize", targetCardGroup.size());

        return true;
    }

    public static void InvokeOnClick(GridCardSelectScreen selectScreen){
        if (onClickCard != null) {
            onClickCard.Invoke(mergedGroup, AbstractDungeon.gridSelectScreen.selectedCards, ClassUtils.GetField(selectScreen, "hoveredCard"));
        }
    }

    public static void UpdateDynamicString() {
        if (dynamicString != null) {
            dynamicLabel.SetText(dynamicString.Invoke(AbstractDungeon.gridSelectScreen.selectedCards)).Autosize(1f, null);
        }
    }

    public static void RenderDynamicString(SpriteBatch sb) {
        if (dynamicString != null) {
            //dynamicLabel.SetText(dynamicString.Invoke(AbstractDungeon.gridSelectScreen.selectedCards));
            dynamicLabel.Render(sb);
        }
    }
}
